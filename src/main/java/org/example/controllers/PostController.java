package org.example.controllers;

import org.example.DTO.PostDTO;
import org.example.models.Post;
import org.example.models.PostElastic;
import org.example.services.KafkaConsumer;
import org.example.services.PostElasticService;
import org.example.services.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// необходимо id не руками передавать
@RestController
@RequestMapping("/posts")
public class PostController {
    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    PostService postService;
    PostElasticService postElasticService;

    KafkaConsumer kafkaConsumer;

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
    @GetMapping("")
    public ResponseEntity<?> check() {
        return ResponseEntity.ok().body("доступ получен");
    }
    @PostMapping("/create")
    public  ResponseEntity<?> createPost(@RequestBody PostDTO postDTO) {
        try {
            System.out.println(postDTO.getContent());
            postElasticService.createPostElastic(postElasticService.fromPostDTOToPostElastic(postDTO));
            postService.createPost(postService.fromPostDTOToPost(postDTO));
            return ResponseEntity.ok().body("Пост сохранен");
        }  catch (Exception e) {
            log.error("Ошибка при получении владельца", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }

    }

    @GetMapping("/search")
    public ResponseEntity<?> findPosts(@RequestParam("pattern") String pattern,
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
    public ResponseEntity<?>  deletePost(@RequestParam Long id) {
        try {
            postService.deletePost(id);
            postElasticService.deletePost(id);
            return ResponseEntity.ok().body("Пост удален");
        }
        catch (Exception e) {
            log.error("Ошибка при получении владельца", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?>  updatePost(@RequestBody PostDTO postDTO) {
        try {
            postService.updatePost(postService.fromPostDTOToPost(postDTO));
            postElasticService.updatePostElastic(postElasticService.fromPostDTOToPostElastic(postDTO));
            return ResponseEntity.ok().body("Пост сохранен");
        }  catch (Exception e) {
            log.error("Ошибка при получении владельца", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }
    }

    @Autowired
    public void setKafkaConsumer(KafkaConsumer kafkaConsumer) { this.kafkaConsumer = kafkaConsumer;}

    @Autowired
    public void setPostService(PostService postService) {
        this.postService = postService;
    }

    @Autowired
    public void setPostElasticService(PostElasticService postElasticService) {this.postElasticService = postElasticService;}
}
