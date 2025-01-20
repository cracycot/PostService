package unit;

import org.example.DTO.PostDTO;
import org.example.models.Image;
import org.example.models.Post;
import org.example.repositories.elastic.PostElasticRepository;
import org.example.repositories.jpa.PostJpaRepository;
import org.example.services.PostService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private RedisTemplate<String, PostDTO> postDTORedisTemplate;

    @Mock
    private ValueOperations<String, PostDTO> valueOperations;

    @Mock
    private PostJpaRepository postRepository;

    @Mock
    private PostElasticRepository postElasticRepository;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        // Настраиваем мок RedisTemplate для возврата мок ValueOperations
        Mockito.when(postDTORedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testGetPostById_WhenPostExistsInDB_StandardBehaviour() {
        Long testId = 1L;
        Post mockPost = new Post();
        mockPost.setId(testId);
        mockPost.setTitle("Test title");
        mockPost.setIdOwner(100L);
        mockPost.setContent("Test content");

        Image image1 = new Image();
        image1.setS3url("url1");
        Image image2 = new Image();
        image2.setS3url("url2");
        mockPost.setImages(Arrays.asList(image1, image2));

        // Имитация отсутствия поста в Redis
        Mockito.when(valueOperations.get("post:" + testId))
                .thenReturn(null);

        Mockito.when(postRepository.findByIdWithImages(testId))
                .thenReturn(Optional.of(mockPost));

        Optional<PostDTO> result = postService.getPostById(testId);

        // Проверяем, что пост найден
        Assertions.assertTrue(result.isPresent(), "Результат должен содержать Post");
        Assertions.assertEquals("Test title", result.get().getTitle());
        Assertions.assertEquals(100L, result.get().getIdOwner());
        Assertions.assertEquals("Test content", result.get().getContent());
        Assertions.assertEquals(Arrays.asList("url1", "url2"), result.get().getUrls());

        // Проверяем, что PostDTO был сохранён в Redis
        Mockito.verify(valueOperations, Mockito.times(1))
                .set(
                        ArgumentMatchers.eq("post:" + testId),
                        ArgumentMatchers.any(PostDTO.class),
                        ArgumentMatchers.eq(10L),
                        ArgumentMatchers.eq(TimeUnit.MINUTES)
                );
    }

    @Test
    void testCreatePost_WhenPostExistsInDB_StandardBehaviour() {
        PostDTO postDTO = new PostDTO.Builder()
                .id(10L)
                .idOwner(200L)
                .title("New Post")
                .content("Test content")
                .photos(Arrays.asList("url1", "url2"))
                .build();

        // Имитация сохранения поста в репозиторий
        Mockito.when(postRepository.save(ArgumentMatchers.any(Post.class)))
                .thenAnswer(invocation -> {
                    Post savedPost = invocation.getArgument(0);
                    savedPost.setId(10L);
                    return savedPost;
                });

        postService.createPost(postDTO);

        Mockito.verify(postRepository, Mockito.times(1))
                .save(ArgumentMatchers.any(Post.class));

        ArgumentCaptor<PostDTO> postDTOCaptor = ArgumentCaptor.forClass(PostDTO.class);

        Mockito.verify(valueOperations, Mockito.times(1))
                .set(
                        ArgumentMatchers.eq("post:" + postDTO.getId()),
                        postDTOCaptor.capture(),
                        ArgumentMatchers.eq(10L),
                        ArgumentMatchers.eq(TimeUnit.MINUTES)
                );

        // Проверяем содержимое сохранённого PostDTO
        PostDTO cachedPostDTO = postDTOCaptor.getValue();
        Assertions.assertEquals(postDTO.getId(), cachedPostDTO.getId());
        Assertions.assertEquals(postDTO.getIdOwner(), cachedPostDTO.getIdOwner());
        Assertions.assertEquals(postDTO.getTitle(), cachedPostDTO.getTitle());
        Assertions.assertEquals(postDTO.getContent(), cachedPostDTO.getContent());
        Assertions.assertEquals(new HashSet<>(postDTO.getUrls()), new HashSet<>(cachedPostDTO.getUrls()));
    }
}