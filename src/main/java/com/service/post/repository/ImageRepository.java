package com.service.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.post.entity.ImageEntity;

public interface ImageRepository extends JpaRepository<ImageEntity, String> {
  
}
