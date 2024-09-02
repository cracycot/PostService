package org.example.repositories;

import org.aspectj.apache.bcel.generic.LOOKUPSWITCH;
import org.example.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}