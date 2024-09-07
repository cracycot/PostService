package org.example.services;

import org.example.DTO.PostDTO;
import org.example.models.PostElastic;
import org.example.repositories.PostElasticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostElasticService {
    private PostElasticRepository postElasticRepository;

    private ElasticsearchOperations elasticsearchOperations;

    public List<PostElastic> searchByPattern(String pattern) {
        List<PostElastic> postElasticsTitle = postElasticRepository.findByTitleContaining(pattern);
        for (PostElastic postElastic: postElasticsTitle) {
            System.out.println(postElastic.getContent());
        }
        postElasticsTitle.addAll(postElasticRepository.findByContentContaining(pattern));
        return postElasticsTitle;
    }

    public Optional<PostElastic> getPostById(Long id) {
        return postElasticRepository.findById(id);
    }

    public void createPostElastic(PostElastic post) {
        postElasticRepository.save(post);
    }

    public void updatePostElastic(PostElastic post) {
        postElasticRepository.save(post);
    }

    public void deletePost(Long id) {
        Optional<PostElastic> postElasticOptional = postElasticRepository.findById(id);
        if (postElasticOptional.isPresent()) {
            PostElastic postElastic = postElasticOptional.get();
            postElasticRepository.delete(postElastic);
        } else {
            throw new RuntimeException("Post not found with id: " + id);
        }
    }
    public PostElastic fromPostDTOToPostElastic(PostDTO postDTO) {
        return new PostElastic.Builder()
                .id(postDTO.getId())
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .build();
    }
    public PostDTO fromPostElasticToPostDTO(PostElastic postElastic) { // костыль
        return new PostDTO.Builder()
                .id(postElastic.getId())
                .idOwner(0l)
                .content(postElastic.getContent())
                .title(postElastic.getTitle())
                .build()
                ;
    }
    @Autowired
    private  void setPostElasticRepository(PostElasticRepository postElasticRepository) {
        this.postElasticRepository = postElasticRepository;
    }

    @Autowired
    private  void setElasticsearchOperations(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
}
