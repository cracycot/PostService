package org.example.repositories;

import org.example.models.PostElastic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostElasticRepository extends ElasticsearchRepository<PostElastic, Long> {
    Page<PostElastic> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}
