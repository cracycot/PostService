package org.example.DTO;

import java.util.List;

public class PostDTO {
    private Long id;

    private Long idOwner;
    private String title;
    private String content;

    private List<String> urls;

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

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
    public static class Builder {
        private final PostDTO postDTO = new PostDTO();

        public Builder id(Long id) {
            postDTO.id = id;
            return this;
        }

        public Builder idOwner(Long idOwner) {
            postDTO.idOwner = idOwner;
            return this;
        }

        public Builder title(String title) {
            postDTO.title = title;
            return this;
        }

        public Builder content(String content) {
            postDTO.content = content;
            return this;
        }

        public Builder photos(List<String> urls) {
            postDTO.urls = urls;
            return this;
        }

        public PostDTO build() {
            return postDTO;
        }
    }

}
