package org.example.services;

import org.apache.kafka.common.protocol.types.Field;
import org.example.DTO.PostDTO;
import org.example.models.Post;
import org.example.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class PostService {
    @Autowired
    private RedisTemplate<String, Post> redisTemplate;

    private static final String POST_KEY_PREFIX = "post:";
    PostRepository postRepository;

    public Optional<Post> getPostById(Long id) {
        String redisKey = POST_KEY_PREFIX + id;
        Post post = redisTemplate.opsForValue().get(redisKey);

        if (post != null) {
            return Optional.of(post);
        }

        // Если поста нет в Redis, возьмем его из БД (добавить логику обращения к БД)
        Optional<Post> postFromDb = postRepository.findById(id);

        // Сохраним пост в Redis на 10 минут
        postFromDb.ifPresent(value -> redisTemplate.opsForValue().set(redisKey, value, 10, TimeUnit.MINUTES));

        return postFromDb;
    }

    public void createPost(Post post) {
        String redisKey = POST_KEY_PREFIX + post.getId();
        redisTemplate.opsForValue().set(redisKey, post, 10, TimeUnit.MINUTES);
        postRepository.save(post);
    }

    public void updatePost(Post post) {
        postRepository.save(post);
    }

    public void deletePost(Long id) {
        Optional<Post> postOptional = postRepository.findById(id);
        if (postOptional.isPresent()) {
            String redisKey = POST_KEY_PREFIX + id;
            redisTemplate.delete(redisKey);
            Post post = postOptional.get();
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

    public Post fromPostDTOToPost(PostDTO postDTO){
//        Optional<Post> postOptional = postRepository.findById(postDTO.getIdOwner());
//        if (!postOptional.isPresent()) {
//            throw new RuntimeException("Владелец не найден!");
//        }
//        Post post = postOptional.get();
        return new Post.Builder()
                .id(postDTO.getId())
                .idOwner(postDTO.getIdOwner())
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .build();
    }

    public ArrayList<PostDTO> findByName(String pattern) {
        return new ArrayList<PostDTO>();
    }

    @Autowired
//    @Qualifier("PostRepository")
    // @Qualifier|("PostDAO") //уточненине для спринга если наследуемый интерфейс имеет две реализации в бинах
    public void setPostRepository(PostRepository postRepository ) {
        this.postRepository = postRepository;
    }

}
