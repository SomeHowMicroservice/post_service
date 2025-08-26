package com.service.post.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "topics", indexes = { @Index(name = "topics_slug_key", columnList = "slug", unique = true) })
public class TopicEntity extends BaseEntity {
  @Column(nullable = false, length = 150)
  private String name;

  @Column(nullable = false, length = 150)
  private String slug;

  @Column(name = "is_deleted", nullable = false)
  @Builder.Default
  private boolean deletedTopic = false;

  @Column(nullable = false, columnDefinition = "CHAR(36)")
  private String createdById;

  @Column(nullable = false, columnDefinition = "CHAR(36)")
  private String updatedById;

  @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PostEntity> posts = new ArrayList<>();
}
