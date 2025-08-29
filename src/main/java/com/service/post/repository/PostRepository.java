package com.service.post.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.service.post.entity.PostEntity;

import jakarta.persistence.LockModeType;

public interface PostRepository extends JpaRepository<PostEntity, String>, JpaSpecificationExecutor<PostEntity> {
  boolean existsBySlug(String slug);

  Optional<PostEntity> findByIdAndDeletedPostFalse(String id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM PostEntity p WHERE p.id = :id AND p.deletedPost = false")
  Optional<PostEntity> findByIdForUpdate(@Param("id") String id);
}
