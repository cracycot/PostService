package org.example.repositories;

import org.example.models.PostElastic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostElasticRepository extends ElasticsearchRepository<PostElastic, Long> {
    List<PostElastic> findByTitleContaining(String title);
}
