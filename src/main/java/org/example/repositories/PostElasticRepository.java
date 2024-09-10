package org.example.repositories;

import org.example.models.PostElastic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostElasticRepository extends ElasticsearchRepository<PostElastic, Long> {
//    List<PostElastic> findByTitleContaining(String title); // надо сдлелать pagebale
//    List<PostElastic> findByContentContaining(String content); //как обьединить
    Page<PostElastic> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
    //Page<PostElastic> findByTitleContaining(String title, Pageable pageable);

}
