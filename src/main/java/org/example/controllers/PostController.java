package org.example.controllers;

import org.example.DTO.PostDTO;
import org.example.models.Post;
import org.example.services.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController

public class PostController {
    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    PostService postService;



    @GetMapping("/get")
    public ResponseEntity<?> getPostById(@RequestParam Long id) {
        try {
            Optional<Post> postOptional = postService.getPostById(id);
            if (!postOptional.isPresent()) {
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
            postService.createPost(postService.fromPostDTOToPost(postDTO));
            return ResponseEntity.ok().body("Пост сохранен");
        }  catch (Exception e) {
            log.error("Ошибка при получении владельца", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }

    }

    @GetMapping("/delete")
    public ResponseEntity<?>  deletePost(@RequestParam Long id) {
        try {
            postService.deletePost(id);
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
            return ResponseEntity.ok().body("Пост сохранен");
        }  catch (Exception e) {
            log.error("Ошибка при получении владельца", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }
    }

    @Autowired
    public void setPostService(PostService postService) {
        this.postService = postService;
    }
}
