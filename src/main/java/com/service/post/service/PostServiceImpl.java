package com.service.post.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.service.post.BaseProfileResponse;
import com.service.post.BaseUserResponse;
import com.service.post.CreateImageRequest;
import com.service.post.CreatePostRequest;
import com.service.post.RestoreManyRequest;
import com.service.post.RestoreOneRequest;
import com.service.post.TopicAdminResponse;
import com.service.post.TopicsAdminResponse;
import com.service.post.UpdateTopicRequest;
import com.service.post.common.SlugUtil;
import com.service.post.dto.Base64UploadDto;
import com.service.post.entity.ImageEntity;
import com.service.post.entity.PostEntity;
import com.service.post.entity.TopicEntity;
import com.service.post.exceptions.AlreadyExistsException;
import com.service.post.exceptions.ResourceNotFoundException;
import com.service.post.grpc_clients.UserClient;
import com.service.post.mq.Publisher;
import com.service.post.CreateTopicRequest;
import com.service.post.DeleteManyRequest;
import com.service.post.DeleteOneRequest;
import com.service.post.repository.ImageRepository;
import com.service.post.repository.PostRepository;
import com.service.post.repository.TopicRepository;
import com.service.user.UserPublicResponse;
import com.service.user.UsersPublicResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
  private final TopicRepository topicRepository;
  private final PostRepository postRepository;
  private final ImageRepository imageRepository;
  private final Publisher publish;
  private final UserClient userClient;

  @Value("${spring.imagekit.url_endpoint}")
  private String imageKitUrlEndpoint;

  @Value("${spring.imagekit.folder}")
  private String imageKitFolder;

  @Override
  public String createTopic(CreateTopicRequest request) {
    String slug = request.hasSlug() && !request.getSlug().isEmpty() ? request.getSlug()
        : SlugUtil.toSlug(request.getName());

    if (topicRepository.existsBySlug(slug)) {
      throw new AlreadyExistsException("slug chủ đề đã tồn tại");
    }

    TopicEntity topic = TopicEntity.builder().name(request.getName()).slug(slug).createdById(request.getUserId())
        .updatedById(request.getUserId()).build();
    topicRepository.save(topic);
    return topic.getId();
  }

  @Override
  public TopicsAdminResponse getAllTopicsAdmin() {
    List<TopicEntity> topics = topicRepository.findAllByDeletedTopicIsFalse();

    if (topics.isEmpty()) {
      return TopicsAdminResponse.newBuilder().build();
    }

    Set<String> userIdSet = new HashSet<>();
    for (TopicEntity t : topics) {
      userIdSet.add(t.getCreatedById());
      userIdSet.add(t.getUpdatedById());
    }
    List<String> userIds = new ArrayList<>(userIdSet);

    UsersPublicResponse usersRes = userClient.getUsersById(userIds);

    Map<String, UserPublicResponse> usersMap = usersRes.getUsersList().stream()
        .collect(Collectors.toMap(UserPublicResponse::getId, u -> u));

    TopicsAdminResponse.Builder responseBuilder = TopicsAdminResponse.newBuilder();
    for (TopicEntity t : topics) {
      TopicAdminResponse.Builder topicBuilder = TopicAdminResponse.newBuilder().setId(t.getId()).setName(t.getName())
          .setSlug(t.getSlug()).setCreatedAt(t.getCreatedAt().toString()).setUpdatedAt(t.getUpdatedAt().toString());

      if (usersMap.containsKey(t.getCreatedById())) {
        topicBuilder.setCreatedBy(toBaseUserResponse(usersMap.get(t.getCreatedById())));
      }

      if (usersMap.containsKey(t.getUpdatedById())) {
        topicBuilder.setUpdatedBy(toBaseUserResponse(usersMap.get(t.getUpdatedById())));
      }

      responseBuilder.addTopics(topicBuilder);
    }

    return responseBuilder.build();
  }

  @Override
  public TopicsAdminResponse getDeletedTopics() {
    List<TopicEntity> topics = topicRepository.findAllByDeletedTopicIsTrue();

    if (topics.isEmpty()) {
      return TopicsAdminResponse.newBuilder().build();
    }

    Set<String> userIdSet = new HashSet<>();
    for (TopicEntity t : topics) {
      userIdSet.add(t.getCreatedById());
      userIdSet.add(t.getUpdatedById());
    }
    List<String> userIds = new ArrayList<>(userIdSet);

    UsersPublicResponse usersRes = userClient.getUsersById(userIds);

    Map<String, UserPublicResponse> usersMap = usersRes.getUsersList().stream()
        .collect(Collectors.toMap(UserPublicResponse::getId, u -> u));

    TopicsAdminResponse.Builder responseBuilder = TopicsAdminResponse.newBuilder();
    for (TopicEntity t : topics) {
      TopicAdminResponse.Builder topicBuilder = TopicAdminResponse.newBuilder().setId(t.getId()).setName(t.getName())
          .setSlug(t.getSlug()).setCreatedAt(t.getCreatedAt().toString()).setUpdatedAt(t.getUpdatedAt().toString());

      if (usersMap.containsKey(t.getCreatedById())) {
        topicBuilder.setCreatedBy(toBaseUserResponse(usersMap.get(t.getCreatedById())));
      }

      if (usersMap.containsKey(t.getUpdatedById())) {
        topicBuilder.setUpdatedBy(toBaseUserResponse(usersMap.get(t.getUpdatedById())));
      }

      responseBuilder.addTopics(topicBuilder);
    }

    return responseBuilder.build();
  }

  @Override
  @Transactional
  public void updateTopic(UpdateTopicRequest request) {
    TopicEntity topic = topicRepository.findByIdAndDeletedTopicFalse(request.getId())
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy chủ đề"));

    if (!topic.getName().equals(request.getName())) {
      topic.setName(request.getName());
    }
    if (!topic.getSlug().equals(request.getSlug())) {
      if (topicRepository.existsBySlug(request.getSlug())) {
        throw new AlreadyExistsException("slug đã tồn tại");
      }
      topic.setSlug(request.getSlug());
    }
    if (!topic.getUpdatedById().equals(request.getUserId())) {
      topic.setUpdatedById(request.getUserId());
    }

    topicRepository.save(topic);
  }

  @Override
  public void deleteTopic(DeleteOneRequest request) {
    TopicEntity topic = topicRepository.findByIdAndDeletedTopicFalse(request.getId())
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy chủ đề bài viết"));

    topic.setDeletedTopic(true);

    if (!topic.getUpdatedById().equals(request.getUserId())) {
      topic.setUpdatedById(request.getUserId());
    }

    topicRepository.save(topic);
  }

  @Override
  @Transactional
  public void deleteTopics(DeleteManyRequest request) {
    List<TopicEntity> topics = topicRepository.findAllByIdInAndDeletedTopicFalse(request.getIdsList());
    if (topics.size() != request.getIdsCount()) {
      throw new ResourceNotFoundException("Có chủ đề không tìm thấy");
    }

    topicRepository.updateIsDeletedAllById(request.getIdsList(), true, request.getUserId());
  }

  @Override
  public void restoreTopic(RestoreOneRequest request) {
    TopicEntity topic = topicRepository.findByIdAndDeletedTopicTrue(request.getId())
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy chủ đề bài viết"));

    topic.setDeletedTopic(false);

    if (!topic.getUpdatedById().equals(request.getUserId())) {
      topic.setUpdatedById(request.getUserId());
    }

    topicRepository.save(topic);
  }

  @Override
  @Transactional
  public void restoreTopics(RestoreManyRequest request) {
    List<TopicEntity> topics = topicRepository.findAllByIdInAndDeletedTopicTrue(request.getIdsList());
    if (topics.size() != request.getIdsCount()) {
      throw new ResourceNotFoundException("Có chủ đề không tìm thấy");
    }

    topicRepository.updateIsDeletedAllById(request.getIdsList(), false, request.getUserId());
  }

  @Override
  public void permanentlyDeleteTopic(String topicId) {
    TopicEntity topic = topicRepository.findByIdAndDeletedTopicTrue(topicId)
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy chủ đề"));

    topicRepository.delete(topic);
  }

  @Override
  public void permanentlyDeleteTopics(List<String> topicIds) {
    List<TopicEntity> topics = topicRepository.findAllByIdInAndDeletedTopicTrue(topicIds);
    if (topics.size() != topicIds.size()) {
      throw new ResourceNotFoundException("Có chủ đề không tìm thấy");
    }

    topicRepository.deleteAll(topics);
  }

  @Override
  @Transactional
  public String createPost(CreatePostRequest request) {
    if (!topicRepository.existsById(request.getTopicId())) {
      throw new ResourceNotFoundException("không tìm thấy chủ đề");
    }

    String slug = SlugUtil.toSlug(request.getTitle());

    if (postRepository.existsBySlug(slug)) {
      throw new AlreadyExistsException("slug bài viết đã tồn tại");
    }

    LocalDateTime publishedAt = request.getIsPublished() ? LocalDateTime.now() : null;

    List<ImageEntity> images = new ArrayList<>();

    for (CreateImageRequest img : request.getImagesList()) {
      String ext = getExtension(img.getFileName()).toLowerCase();
      if (ext.isEmpty()) {
        ext = ".jpg";
      }

      String fileName = String.format("%s-image%d%s", slug, img.getSortOrder(), ext);
      String imageUrl = String.format("%s/%s/%s", imageKitUrlEndpoint, imageKitFolder, fileName);

      ImageEntity image = ImageEntity.builder().url(imageUrl).sortOrder(img.getSortOrder()).build();
      imageRepository.save(image);
      images.add(image);

      Base64UploadDto uploadImageRequest = Base64UploadDto.builder().imageId(image.getId())
          .base64Data(img.getBase64Data()).fileName(fileName).folder(imageKitFolder).build();

      publish.sendUploadImage(uploadImageRequest);
    }

    PostEntity post = PostEntity.builder().title(request.getTitle()).slug(slug).content(request.getContent())
        .publishedPost(request.getIsPublished()).publishedAt(publishedAt).createdById(request.getUserId())
        .updatedById(request.getUserId()).images(images).build();

    postRepository.save(post);

    return post.getId();
  }

  private static String getExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
      return fileName.substring(lastDotIndex);
    }
    return "";
  }

  private BaseUserResponse toBaseUserResponse(UserPublicResponse u) {
    return BaseUserResponse.newBuilder().setId(u.getId()).setUsername(u.getUsername())
        .setProfile(BaseProfileResponse.newBuilder().setId(u.getProfile().getId())
            .setFirstName(u.getProfile().getFirstName()).setLastName(u.getProfile().getLastName()).build())
        .build();
  }
}
