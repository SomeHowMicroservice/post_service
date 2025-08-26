package com.service.post.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.service.post.entity.TopicEntity;

@Repository
public interface TopicRepository extends JpaRepository<TopicEntity, String> {
  boolean existsBySlug(String slug);

  Optional<TopicEntity> findByIdAndDeletedTopicFalse(String id);

  Optional<TopicEntity> findByIdAndDeletedTopicTrue(String id);

  List<TopicEntity> findAllByIdInAndDeletedTopicTrue(List<String> ids);

  List<TopicEntity> findAllByIdInAndDeletedTopicFalse(List<String> ids);

  List<TopicEntity> findAllByDeletedTopicIsFalse();

  List<TopicEntity> findAllByDeletedTopicIsTrue();

  @Modifying
  @Query("UPDATE TopicEntity t SET t.deletedTopic = :isDeleted, t.updatedById = :updatedById WHERE t.id IN :ids")
  void updateIsDeletedAllById(@Param("ids") List<String> ids, @Param("isDeleted") boolean isDeleted, @Param("updatedById") String updatedById);
}
