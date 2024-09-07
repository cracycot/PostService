package org.example.services;

import org.apache.kafka.common.protocol.types.Field;
import org.example.DTO.PostDTO;
import org.example.models.Post;
import org.example.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class PostService {
    PostRepository postRepository;

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public void createPost(Post post) {
        postRepository.save(post);
    }

    public void updatePost(Post post) {
        postRepository.save(post);
    }

    public void deletePost(Long id) {
        Optional<Post> postOptional = postRepository.findById(id);
        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            postRepository.delete(post);
        } else {
            throw new RuntimeException("Post not found with id: " + id);
        }
    }
    public PostDTO fromPostToPostDTO(Post post) {
        return new PostDTO.Builder()
                .id(post.getId())
                .idOwner(post.getIdOwner())
                .title(post.getTitle())
                .content(post.getContent())
                .build();
    }

    public Post fromPostDTOToPost(PostDTO postDTO){
//        Optional<Post> postOptional = postRepository.findById(postDTO.getIdOwner());
//        if (!postOptional.isPresent()) {
//            throw new RuntimeException("Владелец не найден!");
//        }
//        Post post = postOptional.get();
        return new Post.Builder()
                .id(postDTO.getId())
                .idOwner(postDTO.getIdOwner())
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .build();
    }

    public ArrayList<PostDTO> findByName(String pattern) {
        return new ArrayList<PostDTO>();
    }

    @Autowired
//    @Qualifier("PostRepository")
    // @Qualifier|("PostDAO") //уточненине для спринга если наследуемый интерфейс имеет две реализации в бинах
    public void setPostRepository(PostRepository postRepository ) {
        this.postRepository = postRepository;
    }

}
