package org.example.controllers;

import org.example.DTO.PostDTO;
import org.example.models.Post;
import org.example.models.PostElastic;
import org.example.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

// необходимо id не руками передавать
@RestController
@RequestMapping("/posts")
public class PostController {
    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    PostService postService;
    PostElasticService postElasticService;

    KafkaConsumer kafkaConsumer;
    KafkaProducer kafkaProducer;

    @GetMapping("/get")
    public ResponseEntity<?> getPostById(@RequestParam("id") Long id) {
        try {
            Optional<Post> postOptional = postService.getPostById(id);
            if (postOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пост не найден");
            }
            Post post = postOptional.get();
            return ResponseEntity.ok().body(postService.fromPostToPostDTO(post));
        } catch (Exception e) {
            log.error("Ошибка при получении владельца", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }
    }


    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody PostDTO postDTO) {
        try {
            postElasticService.createPostElastic(postElasticService.fromPostDTOToPostElastic(postDTO));
            postService.createPost(postDTO);
            kafkaProducer.sendMessage(MessageBuilder.madeMessageAddPointsForUser(postDTO.getIdOwner(), 1));
            return ResponseEntity.ok().body("Пост сохранен");
        } catch (Exception e) {
            log.error("Ошибка при получении владельца", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }

    }

    // TODO: Выполнить глубокий рефакторинг метода
    @GetMapping("/search")
    public ResponseEntity<Page<PostElastic>> findPosts(@RequestParam(value = "pattern", required = false) String pattern,
                                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                                   @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostElastic> postElasticPage = postElasticService.searchByPattern(pattern, pageable);

        // Преобразование содержимого из PostElastic в PostDTO
//        List<PostDTO> postDTOList = postElasticPage.getContent().stream()
//                .map(postElasticService::fromPostElasticToPostDTO)
//                .collect(Collectors.toList());

        //PaginatedResponse<PostDTO> paginatedResponse = postElasticService.convertToPaginatedResponse(postElasticPage);

        // Преобразование Page<PostDTO> в PagedModel
        Page<PostDTO> postDTOS = postElasticService.convertToPagePostDTO(postElasticPage);
        return ResponseEntity.ok().body(postElasticPage); // костыль
    }

    @GetMapping("/delete")
    public ResponseEntity<String> deletePost(@RequestParam Long id) {
        try {
            Optional<Post> post = postService.getPostById(id);
            if (post.isEmpty()) {
                throw new Exception("Пост не найден");
            }
            postService.deletePost(id);
            postElasticService.deletePost(id);
            kafkaProducer.sendMessage(MessageBuilder.madeMessageRemovePointsForUser(post.get().getId(), 1));
            return ResponseEntity.ok().body("Пост удален");
        } catch (Exception e) {
            log.error("Ошибка при получении владельца", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updatePost(@RequestBody PostDTO postDTO) {
        try {
            postService.updatePost(postDTO);
            postElasticService.updatePostElastic(postElasticService.fromPostDTOToPostElastic(postDTO));
            return ResponseEntity.ok().body("Пост сохранен");
        } catch (Exception e) {
            log.error("Ошибка при получении владельца", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }
    }

    @Autowired
    public void setKafkaConsumer(KafkaConsumer kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    @Autowired
    public void setPostService(PostService postService) {
        this.postService = postService;
    }

    @Autowired
    public void setPostElasticService(PostElasticService postElasticService) {
        this.postElasticService = postElasticService;
    }

    @Autowired
    public void setKafkaProducer(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }
}
