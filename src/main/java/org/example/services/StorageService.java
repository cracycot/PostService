package org.example.services;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.example.utils.S3Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;



@Service
public class StorageService {

    /**
     * Внутренний класс для создания реализации MultipartFile
     */
    public static class CustomMultipartFile implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public CustomMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = Objects.requireNonNull(content);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content.clone();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }

    AmazonS3 s3Client;

    S3Config s3Config;

    @Value("${aws.s3.bucket}")
    String bucketName;

    /**
     * Метод для сохранения фотографий в S3.
     * @param photos Список файлов фотографий.
     * @return Список URL-адресов загруженных фотографий.
     */
    public List<String> savePhotos(List<MultipartFile> photos) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile photo : photos) {
            String fileName = generateUniqueFileName(Objects.requireNonNull(photo.getOriginalFilename()));
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(photo.getSize());
                s3Client.putObject(new PutObjectRequest(bucketName, fileName, photo.getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.Private));
            }
            catch (IOException e) {
                throw new RuntimeException("Не удалось загрузить фотографию: " + photo.getOriginalFilename(), e);
            }

        }
        return urls;
    }

    public List<MultipartFile> loadPhotos(List<String> urls) {
        ArrayList<MultipartFile> photos = new ArrayList<>();
        for (String url : urls) {
            try {
                String objectKey = getObjectKeyFromUrl(url);
                S3Object s3Object = s3Client.getObject(bucketName, objectKey);
                byte[] content = s3Object.getObjectContent().readAllBytes();
                String fileName = objectKey.substring(objectKey.indexOf('_') + 1);
                String contentType = s3Object.getObjectMetadata().getContentType();

                MultipartFile multipartFile = new CustomMultipartFile(
                        "file", // Имя поля
                        fileName, // Оригинальное имя файла
                        contentType, // Тип контента
                        content // Содержимое
                );
                photos.add(multipartFile);
            } catch (IOException e) {
                throw new RuntimeException("Не удалось загрузить фотографию по URL: " + url, e);
            }
        }
        return photos;
    }

    public void deletePhotos(List<String> urls) {
        for (String url : urls) {
            String objectKey = getObjectKeyFromUrl(url);
            s3Client.deleteObject(bucketName, objectKey);
        }
    }

    /**
     * Генерация уникального имени файла для предотвращения коллизий.
     * @param originalFilename Оригинальное имя файла.
     * @return Уникальное имя файла.
     */
    private String generateUniqueFileName(String originalFilename) {
        return UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("\\s+", "_");
    }

    /**
     * Вспомогательный метод для извлечения ключа объекта из полного URL.
     *
     * @param url Полный URL объекта в S3.
     * @return Ключ объекта.
     */
    private String getObjectKeyFromUrl(String url) {
        try {
            java.net.URL s3Url = new java.net.URL(url);
            String path = s3Url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("Неверный формат URL: " + url, e);
        }
    }

    StorageService(@Autowired S3Config s3Config, @Autowired AmazonS3 amazonS3) {
        this.s3Config = s3Config;
        this.s3Client = amazonS3;
    }
}
