package org.example.services;

import org.example.DTO.PostDTO;
import org.example.models.PostElastic;
import org.example.pagination.PaginatedResponse;
import org.example.repositories.PostElasticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostElasticService {
    private PostElasticRepository postElasticRepository;

    private ElasticsearchOperations elasticsearchOperations;

    public Page<PostElastic> searchByPattern(String pattern, Pageable pageable) {
        return postElasticRepository.findByTitleContainingOrContentContaining(pattern, pattern, pageable);
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
                .idOwner(postDTO.getIdOwner())
                .content(postDTO.getContent())
                .build();
    }

    public PostDTO fromPostElasticToPostDTO(PostElastic postElastic) { // костыль
        return new PostDTO.Builder()
                .id(postElastic.getId())
                .idOwner(postElastic.getIdOwner())
                .content(postElastic.getContent())
                .title(postElastic.getTitle())
                .build()
                ;
    }

//    public PaginatedResponse<PostDTO> convertToPaginatedResponse(Page<PostElastic> page) {
//        List<PostDTO> postDTOs = page.stream()
//                .map(this::fromPostElasticToPostDTO)
//                .collect(Collectors.toList());
//        for (PostElastic postElastic : page) {
//            System.out.println(postElastic.getContent());
//        }
//        return new PaginatedResponse<>(
//                postDTOs,
//                page.getNumber(),
//                page.getSize(),
//                page.getTotalElements(),
//                page.getTotalPages()
//        );
//    }
    public Page<PostDTO> convertToPagePostDTO(Page<PostElastic> postElasticPage) {
        return postElasticPage.map(postElastic -> {
            // Преобразование PostElastic в PostDTO
            return new PostDTO.Builder()
                    .id(postElastic.getId())
                    .idOwner(postElastic.getIdOwner())  // Если у тебя нет idOwner в PostElastic, можно поставить заглушку
                    .title(postElastic.getTitle())
                    .content(postElastic.getContent())
                    .build();
        });
    }

    @Autowired
    private void setPostElasticRepository(PostElasticRepository postElasticRepository) {
        this.postElasticRepository = postElasticRepository;
    }

    @Autowired
    private void setElasticsearchOperations(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
}
