package org.example.services;

import org.example.DTO.PostDTO;
import org.example.DTO.PostElasticDTO;
import org.example.models.Image;
import org.example.models.Post;
import org.example.repositories.elastic.PostElasticRepository;
import org.example.repositories.jpa.PostJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

@Service
public class PostService {
    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    private static final String POST_KEY_PREFIX = "post:";

    private final RedisTemplate<String, PostDTO> postDTORedisTemplate;
    private final PostElasticRepository postElasticRepository;
    private final PostJpaRepository postRepository;

    @Autowired
    public PostService(PostJpaRepository postRepository,
                       PostElasticRepository postElasticRepository,
                       RedisTemplate<String, PostDTO> postDTORedisTemplate) {
        this.postDTORedisTemplate = postDTORedisTemplate;
        this.postRepository = postRepository;
        this.postElasticRepository = postElasticRepository;
    }


    @Transactional(readOnly = true)
    public Optional<PostDTO> getPostById(Long id) {
        String redisKey = getRedisKey(id);
        PostDTO cachedPostDTO = postDTORedisTemplate.opsForValue().get(redisKey);
        if (cachedPostDTO != null) {
            log.info("Пост найден в кэше: {}", id);
            return Optional.of(cachedPostDTO);
        }

        // Получение Post с инициализированными изображениями
        Optional<Post> postOptional = postRepository.findByIdWithImages(id);
        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            PostDTO postDTO = fromPostToPostDTO(post);
            // Кэширование PostDTO
            postDTORedisTemplate.opsForValue().set(redisKey, postDTO, 10, TimeUnit.MINUTES);
            log.info("Пост сохранён в кэше: {}", id);
            return Optional.of(postDTO);
        }

        return Optional.empty();
    }

    public void createPost(PostDTO postDTO) {
        Post postToSave = fromPostDTOToPost(postDTO, new ArrayList<>());
        List<String> photoUrls = savePhotosAndLinkToPost(postDTO, postToSave);

        savePostToDatabaseAndCache(postToSave);
        savePostToElasticsearch(postToSave, photoUrls);

        log.info("Пост создан: {}", postToSave.getId());
    }

    public void updatePost(PostDTO postDTO) {
        Post existingPost = fetchPostByIdOrThrow(postDTO.getId());
        updatePostFields(existingPost, postDTO);

        savePostToDatabaseAndCache(existingPost);
        savePostToElasticsearch(existingPost, existingPost.getUrls());

        log.info("Пост обновлён: {}", existingPost.getId());
    }

    public void deletePost(Long id) {
        Post post = fetchPostByIdOrThrow(id);

        deletePostFromElasticsearch(id);
        deletePostFromCache(id);
        postRepository.delete(post);

        log.info("Пост удалён: {}", id);
    }

    private void deletePostFromCache(Long id) {
        String redisKey = getRedisKey(id);
        postDTORedisTemplate.delete(redisKey);
        log.info("Пост удалён из кэша: {}", id);
    }

    public Page<PostDTO> searchByPattern(String pattern, Pageable pageable) {
        // Поиск в Elasticsearch
        Page<PostElasticDTO> elasticResults = postElasticRepository.findByTitleContainingOrContentContaining(pattern, pattern, pageable);

        // Преобразование результатов в PostDTO
        return elasticResults.map(this::fromElasticDTOToPostDTO);
    }

    private void savePostToDatabaseAndCache(Post post) {
        postRepository.save(post);
        PostDTO postDTO = fromPostToPostDTO(post);
        cachePostDTO(postDTO);
    }

    private void cachePostDTO(PostDTO postDTO) {
        String redisKey = getRedisKey(postDTO.getId());
        postDTORedisTemplate.opsForValue().set(redisKey, postDTO, 10, TimeUnit.MINUTES);
        log.info("Пост закэширован: {}", postDTO.getId());
    }

    private String getRedisKey(Long id) {
        return POST_KEY_PREFIX + id;
    }


    private void savePostToElasticsearch(Post post, List<String> photoUrls) {
        PostElasticDTO elasticDTO = toElasticDTO(post, photoUrls);
        postElasticRepository.save(elasticDTO);
    }

    private void updatePostFields(Post post, PostDTO postDTO) {
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        ArrayList<Image> images = new ArrayList<>();
        for (String url : postDTO.getUrls()) {
            Image image = new Image();
            image.setS3url(url);
            image.setPost(post);
            images.add(image);
        }
        post.setImages(images);
    }

    private Post fetchPostByIdOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    private void deletePostFromElasticsearch(Long id) {
        postElasticRepository.findById(Long.toString(id))
                .ifPresent(postElasticRepository::delete);
    }


    private List<String> savePhotosAndLinkToPost(PostDTO postDTO, Post post) {
        return postDTO.getUrls().parallelStream()
                .map(url -> {
                    Image image = new Image();
                    image.setS3url(url);
                    image.setPost(post);
                    post.getImages().add(image);
                    return url;
                })
                .toList();
    }

    private PostElasticDTO toElasticDTO(Post post, List<String> urls) {
        PostElasticDTO dto = new PostElasticDTO();
        dto.setId(Long.toString(post.getId()));
        dto.setIdOwner(post.getIdOwner());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setUrls(urls);
        return dto;
    }

    private Post fromPostDTOToPost(PostDTO postDTO, List<Image> images) {
        return new Post.Builder()
                .idOwner(postDTO.getIdOwner())
                .title(postDTO.getTitle())
                .images(images)
                .content(postDTO.getContent())
                .build();
    }

        public PostDTO fromPostToPostDTO(Post post) {
        return new PostDTO.Builder()
                .id(post.getId())
                .idOwner(post.getIdOwner())
                .title(post.getTitle())
                .photos(post.getUrls())
                .content(post.getContent())
                .build();
    }

    private PostDTO fromElasticDTOToPostDTO(PostElasticDTO elasticDTO) {
        return new PostDTO.Builder()
                .id(Long.getLong(elasticDTO.getId()))
                .idOwner(elasticDTO.getIdOwner())
                .title(elasticDTO.getTitle())
                .content(elasticDTO.getContent())
                .photos(elasticDTO.getUrls()) // Загружаем фото из URL
                .build();
    }
}