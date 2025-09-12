package com.service.post.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.service.post.BaseProfileResponse;
import com.service.post.BaseUserResponse;
import com.service.post.CreatePostRequest;
import com.service.post.RestoreManyRequest;
import com.service.post.RestoreOneRequest;
import com.service.post.SimpleImageResponse;
import com.service.post.SimpleTopicResponse;
import com.service.post.TopicAdminResponse;
import com.service.post.TopicResponse;
import com.service.post.TopicsAdminResponse;
import com.service.post.UpdatePostRequest;
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
import com.service.post.redis.RedisService;
import com.service.post.CreateTopicRequest;
import com.service.post.DeleteManyRequest;
import com.service.post.DeleteOneRequest;
import com.service.post.GetAllPostsAdminRequest;
import com.service.post.PaginationMetaResponse;
import com.service.post.PostAdminDetailsResponse;
import com.service.post.PostAdminResponse;
import com.service.post.PostsAdminResponse;
import com.service.post.repository.ImageRepository;
import com.service.post.repository.PostRepository;
import com.service.post.repository.TopicRepository;
import com.service.post.specification.PostSpecification;
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
  private final RedisService redisService;
  private final Publisher publisher;
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

    UsersPublicResponse usersRes = userClient.getUsersPublicById(userIds);

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

    UsersPublicResponse usersRes = userClient.getUsersPublicById(userIds);

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
  public List<TopicEntity> getAllTopics() {
    return topicRepository.findAllByDeletedTopicIsFalse();
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
    TopicEntity topic = topicRepository.findByIdAndDeletedTopicFalse(request.getTopicId())
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy chủ đề"));

    String slug = SlugUtil.toSlug(request.getTitle());

    if (postRepository.existsBySlug(slug)) {
      throw new AlreadyExistsException("tiêu đề bài viết đã tồn tại");
    }

    LocalDateTime publishedAt = request.getIsPublished() ? LocalDateTime.now() : null;

    PostEntity post = PostEntity.builder().title(request.getTitle()).slug(slug).content(request.getContent())
        .topic(topic).publishedPost(request.getIsPublished()).publishedAt(publishedAt).createdById(request.getUserId())
        .updatedById(request.getUserId()).build();

    postRepository.saveAndFlush(post);

    List<ImageEntity> images = new ArrayList<>();

    Document doc = Jsoup.parse(request.getContent());
    Elements imgTags = doc.select("img[src^=data:image]");

    long newImages = imgTags.size();
    String qImgRedisKey = redisService.setKey(post.getId(), ":image:");
    redisService.saveString(qImgRedisKey, String.valueOf(newImages), 1, TimeUnit.MINUTES);

    int sortOrder = 1;
    for (Element img : imgTags) {
      String src = img.attr("src");

      String ext = getExtension(src);

      ImageEntity image = ImageEntity.builder().sortOrder(sortOrder).post(post)
          .thumbnailImage(sortOrder == 1).build();
      imageRepository.saveAndFlush(image);
      images.add(image);

      String fileName = String.format("%s-%s_%d.%s", slug, image.getId(), sortOrder, ext);

      String base64Data = src.substring(src.indexOf(",") + 1);

      String redisKey = redisService.setKey(image.getId(), ":image:");
      redisService.saveString(redisKey, src, 1, TimeUnit.MINUTES);

      Base64UploadDto uploadImageRequest = Base64UploadDto.builder().imageId(image.getId()).fileName(fileName)
          .folder(imageKitFolder).base64Data(base64Data).postId(post.getId()).userId(request.getUserId())
          .totalImages(imgTags.size()).build();
      publisher.sendUploadImage(uploadImageRequest);

      sortOrder++;
    }

    return post.getId();
  }

  @Override
  @Transactional
  public PostsAdminResponse getAllPostsAdmin(GetAllPostsAdminRequest request) {
    int page = request.getPage() > 0 ? request.getPage() - 1 : 0;
    int limit = request.getLimit() > 0 ? request.getLimit() : 10;

    String sortField = (request.getSort() != null && !request.getSort().isEmpty()) ? snakeToCamel(request.getSort())
        : "createdAt";
    Sort.Direction direction = "asc".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;

    Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortField));

    Specification<PostEntity> spec = PostSpecification.notDeleted()
        .and(PostSpecification.hasTitleLike(request.getSearch()))
        .and(PostSpecification.hasTopicId(request.getTopicId()))
        .and(PostSpecification.hasPublished(request.hasIsPublished() ? request.getIsPublished() : null));

    Page<PostEntity> postsPage = postRepository.findAll(spec, pageable);

    List<PostAdminResponse> posts = postsPage.getContent().stream().map(this::toPostAdminResponse).toList();

    PaginationMetaResponse meta = PaginationMetaResponse.newBuilder().setPage(postsPage.getNumber() + 1)
        .setLimit(postsPage.getSize()).setTotal((int) postsPage.getTotalElements())
        .setTotalPages(postsPage.getTotalPages()).setHasPrev(postsPage.hasPrevious()).setHasNext(postsPage.hasNext())
        .build();

    return PostsAdminResponse.newBuilder().addAllPosts(posts).setMeta(meta).build();
  }

  @Override
  @Transactional
  public PostAdminDetailsResponse getPostById(String id) {
    PostEntity post = postRepository.findByIdAndDeletedPostFalse(id)
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy bài viết"));

    Set<String> userIdSet = new HashSet<>();
    userIdSet.add(post.getCreatedById());
    userIdSet.add(post.getUpdatedById());
    List<String> userIds = new ArrayList<>(userIdSet);

    UsersPublicResponse usersRes = userClient.getUsersPublicById(userIds);

    Map<String, UserPublicResponse> usersMap = usersRes.getUsersList().stream()
        .collect(Collectors.toMap(UserPublicResponse::getId, u -> u));

    PostAdminDetailsResponse.Builder postBuilder = PostAdminDetailsResponse.newBuilder().setId(post.getId())
        .setTitle(post.getTitle()).setSlug(post.getSlug()).setContent(post.getContent())
        .setTopic(toSimpleTopicResponse(post.getTopic())).setIsPublished(post.isPublishedPost())
        .setCreatedAt(post.getCreatedAt().toString()).setUpdatedAt(post.getUpdatedAt().toString());
    if (post.getPublishedAt() != null) {
      postBuilder.setPublishedAt(post.getPublishedAt().toString());
    }
    if (usersMap.containsKey(post.getCreatedById())) {
      postBuilder.setCreatedBy(toBaseUserResponse(usersMap.get(post.getCreatedById())));
    }
    if (usersMap.containsKey(post.getUpdatedById())) {
      postBuilder.setUpdatedBy(toBaseUserResponse(usersMap.get(post.getUpdatedById())));
    }

    return postBuilder.build();
  }

  @Override
  public String getPostContentById(String id) {
    PostEntity post = postRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy bài viết"));
    
    return post.getContent();
  }

  @Override
  @Transactional
  public void updatePost(UpdatePostRequest request) {
    PostEntity post = postRepository.findByIdAndDeletedPostFalse(request.getId())
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy bài viết"));

    if (request.hasTitle() && !request.getTitle().isEmpty() && !post.getTitle().equals(request.getTitle())) {
      String slug = SlugUtil.toSlug(request.getTitle());

      if (postRepository.existsBySlug(slug)) {
        throw new AlreadyExistsException("tiêu đề bài viết đã tồn tại");
      }

      post.setTitle(request.getTitle());
      post.setSlug(slug);
    }

    if (request.hasContent() && !request.getContent().isEmpty() && !post.getContent().equals(request.getContent())) {
      post.setContent(request.getContent());

      List<ImageEntity> oldImages = imageRepository.findByPostIdOrderBySortOrderAsc(post.getId());

      Document doc = Jsoup.parse(request.getContent());
      Elements imgTags = doc.select("img[src]");

      long totalNewImages = imgTags.stream().filter(img -> img.attr("src").startsWith("data:image")).count();

      String qImgRedisKey = redisService.setKey(request.getId(), ":image:");
      redisService.saveString(qImgRedisKey, String.valueOf(totalNewImages), 1, TimeUnit.MINUTES);

      int sortOrder = 1;
      Set<String> usedImageIds = new HashSet<>();
      String slug = post.getSlug();

      for (Element img : imgTags) {
        String src = img.attr("src");
        if (src.startsWith("https")) {
          ImageEntity existing = oldImages.stream().filter(imageEntity -> src.equals(imageEntity.getUrl())).findFirst()
              .orElse(null);
          if (existing != null) {
            existing.setSortOrder(sortOrder);
            existing.setThumbnailImage(sortOrder == 1);
            imageRepository.save(existing);
            usedImageIds.add(existing.getId());
          }
        } else if (src.startsWith("data:image")) {
          ImageEntity newImage = ImageEntity.builder().sortOrder(sortOrder).thumbnailImage(sortOrder == 1).post(post)
              .build();
          imageRepository.save(newImage);

          String ext = getExtension(src);
          String fileName = String.format("%s-%s_%d.%s", slug, newImage.getId(), sortOrder, ext);
          String base64Data = src.substring(src.indexOf(",") + 1);

          String redisKey = redisService.setKey(newImage.getId(), ":image:");
          redisService.saveString(redisKey, src, 1, TimeUnit.MINUTES);

          Base64UploadDto uploadImageRequest = Base64UploadDto.builder().imageId(newImage.getId()).fileName(fileName)
              .folder(imageKitFolder).base64Data(base64Data).postId(post.getId()).totalImages((int) totalNewImages)
              .userId(request.getUserId()).build();

          publisher.sendUploadImage(uploadImageRequest);

          usedImageIds.add(newImage.getId());
        }

        sortOrder++;
      }

      for (ImageEntity oldImage : oldImages) {
        if (!usedImageIds.contains(oldImage.getId())) {
          imageRepository.delete(oldImage);

          publisher.sendDeleteImage(oldImage.getFileId());
        }
      }
    }

    if (request.hasTopicId() && !request.getTopicId().isEmpty()
        && !post.getTopic().getId().equals(request.getTopicId())) {
      TopicEntity topic = topicRepository.findByIdAndDeletedTopicFalse(request.getTopicId())
          .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy chủ đề"));

      post.setTopic(topic);
    }

    if (request.hasIsPublished() && post.isPublishedPost() != request.getIsPublished()) {
      post.setPublishedPost(request.getIsPublished());
      if (request.getIsPublished()) {
        post.setPublishedAt(LocalDateTime.now());
      } else {
        post.setPublishedAt(null);
      }
    }

    if (!post.getUpdatedById().equals(request.getUserId())) {
      post.setUpdatedById(request.getUserId());
    }

    postRepository.save(post);
  }

  @Override
  public void deletePost(DeleteOneRequest request) {
    PostEntity post = postRepository.findByIdAndDeletedPostFalse(request.getId())
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy bài viết"));

    post.setDeletedPost(true);

    if (!post.getUpdatedById().equals(request.getUserId())) {
      post.setUpdatedById(request.getUserId());
    }

    postRepository.save(post);
  }

  @Override
  @Transactional
  public void deletePosts(DeleteManyRequest request) {
    List<PostEntity> posts = postRepository.findAllByIdInAndDeletedPostFalse(request.getIdsList());
    if (posts.size() != request.getIdsCount()) {
      throw new ResourceNotFoundException("Có bài viết không tìm thấy");
    }

    postRepository.updateIsDeletedAllById(request.getIdsList(), true, request.getUserId());
  }

  @Override
  public void restorePost(RestoreOneRequest request) {
    PostEntity post = postRepository.findByIdAndDeletedPostTrue(request.getId())
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy bài viết"));

    post.setDeletedPost(false);

    if (!post.getUpdatedById().equals(request.getUserId())) {
      post.setUpdatedById(request.getUserId());
    }

    postRepository.save(post);
  }

  @Override
  @Transactional
  public void restorePosts(RestoreManyRequest request) {
    List<PostEntity> posts = postRepository.findAllByIdInAndDeletedPostTrue(request.getIdsList());
    if (posts.size() != request.getIdsCount()) {
      throw new ResourceNotFoundException("Có bài viết không tìm thấy");
    }

    postRepository.updateIsDeletedAllById(request.getIdsList(), false, request.getUserId());
  }

  @Override
  @Transactional
  public void permanentlyDeletePost(String postId) {
    PostEntity post = postRepository.findByIdAndDeletedPostTrue(postId)
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy bài viết"));

    post.getImages().stream().map(ImageEntity::getFileId).forEach(publisher::sendDeleteImage);

    postRepository.delete(post);
  }

  @Override
  @Transactional
  public void permanentlyDeletePosts(List<String> postIds) {
    List<PostEntity> posts = postRepository.findAllByIdInAndDeletedPostTrue(postIds);
    if (posts.size() != postIds.size()) {
      throw new ResourceNotFoundException("Có bài viết không tìm thấy");
    }

    posts.stream().flatMap(post -> post.getImages().stream()).map(ImageEntity::getFileId)
        .forEach(publisher::sendDeleteImage);

    postRepository.deleteAll(posts);
  }

  @Override
  @Transactional
  public PostsAdminResponse getDeletedPosts(GetAllPostsAdminRequest request) {
    int page = request.getPage() > 0 ? request.getPage() - 1 : 0;
    int limit = request.getLimit() > 0 ? request.getLimit() : 10;

    String sortField = (request.getSort() != null && !request.getSort().isEmpty()) ? snakeToCamel(request.getSort())
        : "createdAt";
    Sort.Direction direction = "asc".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;

    Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortField));

    Specification<PostEntity> spec = PostSpecification.isDeleted()
        .and(PostSpecification.hasTitleLike(request.getSearch()))
        .and(PostSpecification.hasTopicId(request.getTopicId()))
        .and(PostSpecification.hasPublished(request.hasIsPublished() ? request.getIsPublished() : null));

    Page<PostEntity> postsPage = postRepository.findAll(spec, pageable);

    List<PostAdminResponse> posts = postsPage.getContent().stream().map(this::toPostAdminResponse).toList();

    PaginationMetaResponse meta = PaginationMetaResponse.newBuilder().setPage(postsPage.getNumber() + 1)
        .setLimit(postsPage.getSize()).setTotal((int) postsPage.getTotalElements())
        .setTotalPages(postsPage.getTotalPages()).setHasPrev(postsPage.hasPrevious()).setHasNext(postsPage.hasNext())
        .build();

    return PostsAdminResponse.newBuilder().addAllPosts(posts).setMeta(meta).build();
  }

  @Override
  @Transactional
  public PostAdminDetailsResponse getDeletedPostById(String id) {
    PostEntity post = postRepository.findByIdAndDeletedPostTrue(id)
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy bài viết"));

    Set<String> userIdSet = new HashSet<>();
    userIdSet.add(post.getCreatedById());
    userIdSet.add(post.getUpdatedById());
    List<String> userIds = new ArrayList<>(userIdSet);

    UsersPublicResponse usersRes = userClient.getUsersPublicById(userIds);

    Map<String, UserPublicResponse> usersMap = usersRes.getUsersList().stream()
        .collect(Collectors.toMap(UserPublicResponse::getId, u -> u));

    PostAdminDetailsResponse.Builder postBuilder = PostAdminDetailsResponse.newBuilder().setId(post.getId())
        .setTitle(post.getTitle()).setSlug(post.getSlug()).setContent(post.getContent())
        .setTopic(toSimpleTopicResponse(post.getTopic())).setIsPublished(post.isPublishedPost())
        .setCreatedAt(post.getCreatedAt().toString()).setUpdatedAt(post.getUpdatedAt().toString());
    if (post.getPublishedAt() != null) {
      postBuilder.setPublishedAt(post.getPublishedAt().toString());
    }
    if (usersMap.containsKey(post.getCreatedById())) {
      postBuilder.setCreatedBy(toBaseUserResponse(usersMap.get(post.getCreatedById())));
    }
    if (usersMap.containsKey(post.getUpdatedById())) {
      postBuilder.setUpdatedBy(toBaseUserResponse(usersMap.get(post.getUpdatedById())));
    }

    return postBuilder.build();
  }

  private String getExtension(String base64Src) {
    String mimeType = base64Src.substring(base64Src.indexOf(":") + 1, base64Src.indexOf(";"));
    String ext = mimeType.substring(mimeType.indexOf("/") + 1);

    return ext;
  }

  private String snakeToCamel(String field) {
    StringBuilder result = new StringBuilder();
    boolean upperNext = false;

    for (char c : field.toCharArray()) {
      if (c == '_') {
        upperNext = true;
      } else {
        result.append(upperNext ? Character.toUpperCase(c) : c);
        upperNext = false;
      }
    }

    return result.toString();
  }

  private SimpleTopicResponse toSimpleTopicResponse(TopicEntity topic) {
    return SimpleTopicResponse.newBuilder().setId(topic.getId()).setName(topic.getName())
        .setIsDeleted(topic.isDeletedTopic()).build();
  }

  private PostAdminResponse toPostAdminResponse(PostEntity post) {
    TopicResponse topic = TopicResponse.newBuilder().setId(post.getTopic().getId()).setName(post.getTopic().getName())
        .setSlug(post.getTopic().getSlug()).build();

    ImageEntity thumb = post.getImages().stream().filter(ImageEntity::isThumbnailImage).findFirst().orElse(
        post.getImages().stream().sorted(Comparator.comparingInt(ImageEntity::getSortOrder)).findFirst().orElse(null));

    SimpleImageResponse thumbResponse = thumb != null
        ? SimpleImageResponse.newBuilder().setId(thumb.getId()).setUrl(thumb.getUrl() != null ? thumb.getUrl() : "")
            .build()
        : SimpleImageResponse.newBuilder().build();

    return PostAdminResponse.newBuilder().setId(post.getId()).setTitle(post.getTitle()).setTopic(topic)
        .setThumbnail(thumbResponse).build();
  }

  private BaseUserResponse toBaseUserResponse(UserPublicResponse u) {
    return BaseUserResponse.newBuilder().setId(u.getId()).setUsername(u.getUsername())
        .setProfile(BaseProfileResponse.newBuilder().setId(u.getProfile().getId())
            .setFirstName(u.getProfile().getFirstName()).setLastName(u.getProfile().getLastName()).build())
        .build();
  }
}
