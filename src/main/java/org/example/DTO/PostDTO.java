package org.example.DTO;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class PostDTO {
    private Long id;

    private Long idOwner;
    private String title;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdOwner() {
        return idOwner;
    }

    public void setIdOwner(Long idOwner) {
        this.idOwner = idOwner;
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

    private String content;

    public static class Builder {
        private static PostDTO postDTO = new PostDTO();

        public PostDTO.Builder id(Long id) {
            postDTO.id = id;
            return this;
        }

        public PostDTO.Builder idOwner(Long idOwner) {
            postDTO.idOwner = idOwner;
            return this;
        }
        public PostDTO.Builder title(String title) {
            postDTO.title = title;
            return this;
        }

        public PostDTO.Builder content(String content) {
            postDTO.content = content;
            return this;
        }

        public PostDTO build() {
            return postDTO;
        }
    }

}
