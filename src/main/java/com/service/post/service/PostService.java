package com.service.post.service;

import java.util.List;

import com.service.post.CreatePostRequest;
import com.service.post.CreateTopicRequest;
import com.service.post.DeleteManyRequest;
import com.service.post.DeleteOneRequest;
import com.service.post.GetAllPostsAdminRequest;
import com.service.post.PostAdminDetailsResponse;
import com.service.post.PostsAdminResponse;
import com.service.post.RestoreManyRequest;
import com.service.post.RestoreOneRequest;
import com.service.post.TopicsAdminResponse;
import com.service.post.UpdatePostRequest;
import com.service.post.UpdateTopicRequest;
import com.service.post.entity.TopicEntity;

public interface PostService {
  String createTopic(CreateTopicRequest request);

  TopicsAdminResponse getAllTopicsAdmin();

  TopicsAdminResponse getDeletedTopics();

  List<TopicEntity> getAllTopics();

  void updateTopic(UpdateTopicRequest request);

  void deleteTopic(DeleteOneRequest request);

  void deleteTopics(DeleteManyRequest request);

  void restoreTopic(RestoreOneRequest request);

  void restoreTopics(RestoreManyRequest request);

  void permanentlyDeleteTopic(String topicId);

  void permanentlyDeleteTopics(List<String> topicIds);

  String createPost(CreatePostRequest request);

  PostsAdminResponse getAllPostsAdmin(GetAllPostsAdminRequest request);

  PostAdminDetailsResponse getPostById(String id);

  void updatePost(UpdatePostRequest request);

  void deletePost(DeleteOneRequest request);
}
