package org.example.repositories;

import org.example.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("postJpaRepository")
public interface PostJpaRepository extends JpaRepository<Post, Long> {
}
