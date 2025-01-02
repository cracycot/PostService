package unit;

import org.example.DTO.PostDTO;
import org.example.models.Post;
import org.example.repositories.PostRepository;
import org.example.services.PostService;
import org.example.services.StorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private RedisTemplate<String, Post> redisTemplate;

    @Mock
    private ValueOperations<String, Post> valueOperations;

    @Mock
    private PostRepository postRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Дополнительная настройка перед каждым тестом (если нужна).
    }

    @Test
    void testGetPostById_WhenPostExistsInDB() {
        Long testId = 1L;
        Post mockPost = new Post();
        mockPost.setId(testId);
        mockPost.setTitle("Test title");

        // Имитируем, что в Redis этого поста нет.
        Mockito.when(redisTemplate.opsForValue().get("post:" + testId))
                .thenReturn(null);

        // А в БД — есть.
        Mockito.when(postRepository.findById(testId))
                .thenReturn(Optional.of(mockPost));

        // Вызываем тестируемый метод
        Optional<Post> result = postService.getPostById(testId);

        // Проверяем, что пост вернулся
        Assertions.assertTrue(
                result.isPresent(),
                "Результат должен содержать Post"
        );
        Assertions.assertEquals(
                "Test title",
                result.get().getTitle()
        );

        // Проверяем, что пост записался в Redis
        Mockito.verify(redisTemplate.opsForValue(), Mockito.times(1))
                .set(
                        ArgumentMatchers.eq("post:" + testId),
                        ArgumentMatchers.eq(mockPost),
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.any()
                );
    }

    @Test
    void testCreatePost() {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(10L);
        postDTO.setTitle("New Post");
        postDTO.setContent("Test content");

        // Мокаем сохранение фото (не ходим в реальный S3)
        Mockito.doReturn(null)
                .when(storageService)
                .savePhotos(postDTO.getPhotos());

        // При сохранении в репозиторий возвращаем объект с тем же ID
        Mockito.when(postRepository.save(ArgumentMatchers.any(Post.class)))
                .thenAnswer(invocation -> {
                    Post savedPost = invocation.getArgument(0);
                    savedPost.setId(10L);
                    return savedPost;
                });

        // Вызываем логику создания поста
        postService.createPost(postDTO);

        // Проверяем, что метод save() в репозитории вызывался 1 раз
        Mockito.verify(postRepository, Mockito.times(1))
                .save(ArgumentMatchers.any(Post.class));

        // Проверяем, что объект также положился в Redis
        Mockito.verify(redisTemplate.opsForValue(), Mockito.times(1))
                .set(
                        ArgumentMatchers.eq("post:" + postDTO.getId()),
                        ArgumentMatchers.any(Post.class),
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.any()
                );
    }
}