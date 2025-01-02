package unit;

import org.example.repositories.PostElasticRepository;
import org.example.services.PostElasticService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

// methodName_StateUnderTest_ExpectedBehavior
@ExtendWith(MockitoExtension.class)
public class PostElasticServiceTest {
    @Mock
    private PostElasticRepository postElasticRepository;

    @InjectMocks
    private PostElasticService postElasticService;

    @Test
    public void deletePost_WhenPostDoesNotExist_ShouldThrowException() {
        Mockito.when(postElasticRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> postElasticService.deletePost(1L)
        );

        // Проверяем сообщение исключения
        Assertions.assertEquals("Post not found with id: 1", exception.getMessage());

        Mockito.verify(postElasticRepository, Mockito.never()).delete(Mockito.any());
    }

}
