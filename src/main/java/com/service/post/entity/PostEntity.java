package com.service.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "posts", indexes = { @Index(name = "posts_slug_key", columnList = "slug", unique = true) })
public class PostEntity extends BaseEntity {
  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, length = 255)
  private String slug;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "is_published", nullable = false)
  @Builder.Default
  private boolean publishedPost = false;

  @Column(nullable = true)
  private LocalDateTime publishedAt;

  @Column(name = "is_deleted", nullable = false)
  @Builder.Default
  private boolean deletedPost = false;

  @Column(nullable = false, columnDefinition = "CHAR(36)")
  private String createdById;

  @Column(nullable = false, columnDefinition = "CHAR(36)")
  private String updatedById;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id", nullable = false)
  private TopicEntity topic;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ImageEntity> images = new ArrayList<>();
}
