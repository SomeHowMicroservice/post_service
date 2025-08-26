package com.service.post.service;

import java.util.List;

import com.service.post.CreatePostRequest;
import com.service.post.CreateTopicRequest;
import com.service.post.DeleteManyRequest;
import com.service.post.DeleteOneRequest;
import com.service.post.RestoreManyRequest;
import com.service.post.RestoreOneRequest;
import com.service.post.TopicsAdminResponse;
import com.service.post.UpdateTopicRequest;

public interface PostService {
  String createTopic(CreateTopicRequest request);

  TopicsAdminResponse getAllTopicsAdmin();

  TopicsAdminResponse getDeletedTopics();

  void updateTopic(UpdateTopicRequest request);

  void deleteTopic(DeleteOneRequest request);

  void deleteTopics(DeleteManyRequest request);

  void restoreTopic(RestoreOneRequest request);

  void restoreTopics(RestoreManyRequest request);

  void permanentlyDeleteTopic(String topicId);

  void permanentlyDeleteTopics(List<String> topicIds);

  String createPost(CreatePostRequest request);
}
