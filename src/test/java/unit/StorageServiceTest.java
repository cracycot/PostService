package unit;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.example.services.StorageService;
import org.example.utils.S3Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class StorageServiceTest {

    @Mock
    private AmazonS3 s3Client;

    @Mock
    private S3Config s3Config;

    @InjectMocks
    private StorageService storageService;

    @Mock
    private MultipartFile mockFile;

    @Captor
    private ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor;

    private static final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageService, "bucketName", BUCKET_NAME);
    }

    @Test
    void savePhotos_StandardBehavior_ShouldUploadFilesAndReturnUrls() throws IOException {
        // Инициализация мока MultipartFile
        String fileName = "testFile.jpg";
        byte[] content = "test content".getBytes();
        Mockito.when(mockFile.getOriginalFilename()).thenReturn(fileName);
        Mockito.when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        Mockito.when(mockFile.getSize()).thenReturn((long) content.length);

        List<MultipartFile> photos = new ArrayList<>();
        photos.add(mockFile);

        // Мокаем поведение s3Client
        Mockito.when(s3Client.putObject(Mockito.any(PutObjectRequest.class)))
                .thenReturn(new PutObjectResult());


        // Действие
        List<String> urls = storageService.savePhotos(photos);

        // Проверка
        Mockito.verify(s3Client, Mockito.times(1)).putObject(putObjectRequestCaptor.capture());
        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();

        Assertions.assertEquals(BUCKET_NAME, capturedRequest.getBucketName());
        Assertions.assertNotNull(urls);
        Assertions.assertEquals(1, urls.size());
        Assertions.assertTrue(urls.get(0).contains(fileName));
    }

    @Test
    void loadPhotos_StandardBehavior_ShouldReturnListOfMultipartFiles() throws IOException {
        // Инициализация
        String fileUrl = "https://test-bucket.s3.amazonaws.com/testFile.jpg";
        String objectKey = "testFile.jpg";
        byte[] content = "test content".getBytes();

        // Мок объекта S3Object
        S3Object s3Object = Mockito.mock(S3Object.class);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);
        S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(byteArrayInputStream, null);
        Mockito.when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);

        // Мок метаданных
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        Mockito.when(s3Object.getObjectMetadata()).thenReturn(metadata);

        Mockito.when(s3Client.getObject(BUCKET_NAME, objectKey)).thenReturn(s3Object);

        List<String> urls = List.of(fileUrl);

        // Действие
        List<MultipartFile> photos = storageService.loadPhotos(urls);

        // Проверка
        Assertions.assertNotNull(photos);
        Assertions.assertEquals(1, photos.size());
        Assertions.assertArrayEquals(content, photos.get(0).getBytes());
        Assertions.assertEquals("text/plain", photos.get(0).getContentType());
    }

    @Test
    void deletePhotos_c_ShouldCallDeleteObjectOnS3Client() {
        // Инициализация
        String fileUrl = "https://test-bucket.s3.amazonaws.com/testFile.jpg";
        List<String> urls = List.of(fileUrl);

        // Действие
        storageService.deletePhotos(urls);

        // Проверка
        Mockito.verify(s3Client, Mockito.times(1)).deleteObject(BUCKET_NAME, "testFile.jpg");
    }
}