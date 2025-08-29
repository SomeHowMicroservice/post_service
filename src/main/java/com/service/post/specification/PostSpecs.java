package com.service.post.specification;

import org.springframework.data.jpa.domain.Specification;

import com.service.post.entity.PostEntity;

public class PostSpecs {
  public static Specification<PostEntity> notDeleted() {
    return (root, query, cb) -> cb.isFalse(root.get("deletedPost"));
  }

  public static Specification<PostEntity> hasTitleLike(String search) {
    return (root, query, cb) -> (search == null || search.isEmpty())
        ? cb.conjunction()
        : cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%");
  }

  public static Specification<PostEntity> hasTopicId(String topicId) {
    return (root, query, cb) -> (topicId == null || topicId.isEmpty())
        ? cb.conjunction()
        : cb.equal(root.get("topic").get("id"), topicId);
  }

  public static Specification<PostEntity> hasPublished(Boolean published) {
    return (root, query, cb) -> (published == null)
        ? cb.conjunction()
        : cb.equal(root.get("publishedPost"), published);
  }

  public static Specification<PostEntity> topicNotDeleted() {
    return (root, query, cb) -> cb.isFalse(root.get("topic").get("deletedTopic"));
  }
}
