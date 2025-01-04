package org.example.models;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "posts")
@Document(indexName = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id")
    private Long idOwner;

    @Field(type = FieldType.Text, name = "title")
    private String title;

    @Field(type = FieldType.Text, name = "content")
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Field(type = FieldType.Object, index = false)
    private List<Image> images = new ArrayList<>();

    public Post() {}

    public Long getIdOwner() {
        return idOwner;
    }

    public void setIdOwner(Long idOwner) {
        this.idOwner = idOwner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<Image> getImages() {
        return images;
    }

    public static class Builder {
        private static Post post = new Post();

        public Builder idOwner(Long idOwner) {
            post.idOwner = idOwner;
            return this;
        }

        public Builder title(String title) {
            post.title = title;
            return this;
        }

        public Builder content(String content) {
            post.content = content;
            return this;
        }

        public Builder images(List<Image> images) {
            post.images = images;
            return this;
        }

        public Post build() {
            return post;
        }
    }

    /**
     * Метод для получения всех Urls
     */
    public List<String> getUrls() {
        if (images == null) {
            return Collections.emptyList();
        }
        return images.stream()
                .map(Image::getS3url)
                .collect(Collectors.toList());
    }
}