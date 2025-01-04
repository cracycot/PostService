package org.example.repositories.elastic;

import org.example.DTO.PostElasticDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostElasticRepository extends ElasticsearchRepository<PostElasticDTO, String> {
    Page<PostElasticDTO> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}