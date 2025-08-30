package com.service.post.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.service.post.entity.PostEntity;

import jakarta.persistence.LockModeType;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, String>, JpaSpecificationExecutor<PostEntity> {
  boolean existsBySlug(String slug);

  Optional<PostEntity> findByIdAndDeletedPostFalse(String id);

  Optional<PostEntity> findByIdAndDeletedPostTrue(String id);

  List<PostEntity> findAllByIdInAndDeletedPostTrue(List<String> ids);

  List<PostEntity> findAllByIdInAndDeletedPostFalse(List<String> ids);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM PostEntity p WHERE p.id = :id AND p.deletedPost = false")
  Optional<PostEntity> findByIdForUpdate(@Param("id") String id);

  @Modifying
  @Query("UPDATE PostEntity p SET p.deletedPost = :isDeleted, p.updatedById = :updatedById WHERE p.id IN :ids")
  void updateIsDeletedAllById(@Param("ids") List<String> ids, @Param("isDeleted") boolean isDeleted,
      @Param("updatedById") String updatedById);
}
