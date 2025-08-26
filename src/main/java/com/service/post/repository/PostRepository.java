package com.service.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.post.entity.PostEntity;

public interface PostRepository extends JpaRepository<PostEntity, String> {
  boolean existsBySlug(String slug);
}
