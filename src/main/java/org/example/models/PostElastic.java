package org.example.models;

import jakarta.persistence.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "posts")
public class PostElastic {
    @Id
    private Long id;
    @Field(type = FieldType.Text, name = "title")
    private String title;
    @Field(type = FieldType.Text, name = "content")
    private String content;
    private Long idOwner;

    public Long getIdOwner() {
        return idOwner;
    }

    public void setIdOwner(Long ownerId) {
        this.idOwner = ownerId;
    }

    @Override
    public String toString() {
        return "PostElastic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
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
        private static PostElastic post = new PostElastic();

        public Builder id(Long id) {
            post.id = id;
            return this;
        }

        public Builder title(String title) {
            post.title = title;
            return this;
        }

        public Builder idOwner(Long idOwner) {
            post.idOwner = idOwner;
            return this;
        }

        public Builder content(String content) {
            post.content = content;
            return this;
        }

        public PostElastic build() {
            return post;
        }
    }
}
