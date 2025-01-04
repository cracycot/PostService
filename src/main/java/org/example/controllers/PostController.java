package org.example.controllers;

import org.example.DTO.PostDTO;
import org.example.models.Post;
import org.example.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/posts")
public class PostController {
    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    PostService postService;

    KafkaConsumer kafkaConsumer;
    KafkaProducer kafkaProducer;

    @GetMapping("/get")
    public ResponseEntity<?> getPostById(@RequestParam("id") Long id) {
        try {
            Optional<PostDTO> postOptional = postService.getPostById(id);
            if (postOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пост не найден");
            }
            PostDTO post = postOptional.get();
            return ResponseEntity.ok().body(post);
        } catch (Exception e) {
            log.error("Ошибка при получении владельца", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }
    }


    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody PostDTO postDTO) {
        try {
            log.info("Получен запрос: {}", postDTO);
            postService.createPost(postDTO);
            kafkaProducer.sendMessage(MessageBuilder.madeMessageAddPointsForUser(postDTO.getIdOwner(), 1));
            return ResponseEntity.ok().body("Пост сохранен");
        } catch (Exception e) {
            log.error("Ошибка при получении владельца", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }

    }

//    // TODO: Выполнить глубокий рефакторинг метода
//    @GetMapping("/search")
//    public ResponseEntity<Page<PostDTO>> findPosts(@RequestParam(value = "pattern", required = false) String pattern,
//                                                   @RequestParam(name = "page", defaultValue = "0") int page,
//                                                   @RequestParam(name = "size", defaultValue = "10") int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<PostDTO> postElasticPage = postService.searchByPattern(pattern, pageable);
//
//        // Преобразование содержимого из PostElastic в PostDTO
////        List<PostDTO> postDTOList = postElasticPage.getContent().stream()
////                .map(postElasticService::fromPostElasticToPostDTO)
////                .collect(Collectors.toList());
//
//        //PaginatedResponse<PostDTO> paginatedResponse = postElasticService.convertToPaginatedResponse(postElasticPage);
//
//        // Преобразование Page<PostDTO> в PagedModel
//        Page<PostDTO> postDTOS = postService.convertToPagePostDTO(postElasticPage);
//        return ResponseEntity.ok().body(postElasticPage); // костыль
//    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostDTO>> findPosts(@RequestParam(value = "pattern", required = false) String pattern,
                                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                                   @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Поиск постов через сервис
        Page<PostDTO> postDTOPage = postService.searchByPattern(pattern, pageable);

        // Возвращаем результат
        return ResponseEntity.ok(postDTOPage);
    }

    @GetMapping("/delete")
    public ResponseEntity<String> deletePost(@RequestParam Long id) {
        try {
            Optional<PostDTO> post = postService.getPostById(id);
            if (post.isEmpty()) {
                throw new Exception("Пост не найден");
            }
            postService.deletePost(id);
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
    public void setKafkaProducer(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }
}
