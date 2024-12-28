package org.example.services;

import org.example.DTO.PostDTO;
import org.example.models.Image;
import org.example.models.Post;
import org.example.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

@Service
public class PostService {
    private final RedisTemplate<String, Post> redisTemplate;
    private final StorageService storageService;
    private final PostRepository postRepository;
    private static final String POST_KEY_PREFIX = "post:";

    public Optional<Post> getPostById(Long id) {
        String redisKey = POST_KEY_PREFIX + id;
        Post post = redisTemplate.opsForValue().get(redisKey);

        if (post != null) {
            return Optional.of(post);
        }

        Optional<Post> postFromDb = postRepository.findById(id);

        // Сохраним пост в Redis на 10 минут
        postFromDb.ifPresent(value ->
                redisTemplate.opsForValue().set(redisKey, value, 10, TimeUnit.MINUTES));

        return postFromDb;
    }

    public void createPost(PostDTO post) {
        String redisKey = POST_KEY_PREFIX + post.getId();
        storageService.savePhotos(post.getPhotos());
        redisTemplate.opsForValue().set(redisKey, this.fromPostDTOToPost(post), 10, TimeUnit.MINUTES);
        postRepository.save(this.fromPostDTOToPost(post));
    }

    public void updatePost(PostDTO postDTO) {
        Optional<Post> existingPostOptional = postRepository.findById(postDTO.getId());
        if (existingPostOptional.isPresent()) {
            Post existingPost = existingPostOptional.get();

            if (postDTO.getPhotos() != null && !postDTO.getPhotos().isEmpty()) {
                // Удаляем старые фотографии
                List<String> oldPhotosUrls = existingPost.getPhotosUrls();
                storageService.deletePhotos(oldPhotosUrls);

                // Сохраняем новые фотографии
                List<String> newPhotosUrls = storageService.savePhotos(postDTO.getPhotos());
                List<Image> newImages = new ArrayList<>();
                for (String url : newPhotosUrls) {
                    Image image = new Image();
                    image.setPost(existingPost); // Устанавливаем связь с Post
                    image.setS3url(url);         // Устанавливаем URL
                    newImages.add(image);        // Добавляем Image в новый список
                }
                existingPost.setImages(newImages); // Обновляем список фотографий в Post
            }

            // Обновляем остальные поля
            existingPost.setTitle(postDTO.getTitle());
            existingPost.setContent(postDTO.getContent());

            // Сохраняем обновленный пост в базе данных
            postRepository.save(existingPost);

            // Обновляем кеш в Redis
            String redisKey = POST_KEY_PREFIX + postDTO.getId();
            redisTemplate.opsForValue().set(redisKey, existingPost, 10, TimeUnit.MINUTES);
        } else {
            throw new RuntimeException("Post not found with id: " + postDTO.getId());
        }
    }

    public void deletePost(Long id) {
        Optional<Post> postOptional = postRepository.findById(id);
        if (postOptional.isPresent()) {
            String redisKey = POST_KEY_PREFIX + id;
            redisTemplate.delete(redisKey);
            Post post = postOptional.get();
            storageService.deletePhotos(post.getPhotosUrls());
            postRepository.delete(post);
        } else {
            throw new RuntimeException("Post not found with id: " + id);
        }
    }

    public PostDTO fromPostToPostDTO(Post post) {
        return new PostDTO.Builder()
                .id(post.getId())
                .idOwner(post.getIdOwner())
                .title(post.getTitle())
                .content(post.getContent())
                .build();
    }

    public Post fromPostDTOToPost(PostDTO postDTO) {
        return new Post.Builder()
                .id(postDTO.getId())
                .idOwner(postDTO.getIdOwner())
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .build();
    }

    public PostService(@Autowired RedisTemplate<String, Post> redisTemplate,
                       @Autowired PostRepository postRepository,
                       @Autowired StorageService storageService) {
        this.redisTemplate = redisTemplate;
        this.postRepository = postRepository;
        this.storageService = storageService;
    }
}
