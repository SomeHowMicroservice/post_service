package com.service.post.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.post.entity.ImageEntity;

public interface ImageRepository extends JpaRepository<ImageEntity, String> {
  List<ImageEntity> findByPostIdOrderBySortOrderAsc(String postId);
}
