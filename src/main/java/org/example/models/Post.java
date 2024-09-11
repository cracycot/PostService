package org.example.models;
import jakarta.persistence.*;

@Entity
@Table(name = "posts") // Укажите имя таблицы, если необходимо
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idOwner;

    private String title;
    private String content;

    // Конструкторы, геттеры и сеттеры
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

    public static class Builder {
        private static Post post = new Post();

        public Builder id(Long id) {
            post.id = id;
            return this;
        }

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

        public Post build() {
            return post;
        }
    }
}