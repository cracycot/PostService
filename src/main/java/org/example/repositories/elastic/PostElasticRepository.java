package org.example.repositories;

import org.example.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository("postElasticRepository")
public interface PostElasticRepository extends ElasticsearchRepository<Post, Long> {
    Page<Post> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}
