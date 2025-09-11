package com.service.post.mq;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.service.post.config.RabbitMQConfig;
import com.service.post.dto.Base64UploadDto;
import com.service.post.dto.ImageUploadedDto;
import com.service.post.entity.ImageEntity;
import com.service.post.entity.PostEntity;
import com.service.post.exceptions.ResourceNotFoundException;
import com.service.post.imagekit.ImageKitService;
import com.service.post.redis.RedisService;
import com.service.post.repository.ImageRepository;
import com.service.post.repository.PostRepository;

import io.imagekit.sdk.models.results.Result;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Consumer {
  ImageKitService imageKitService;
  ImageRepository imageRepository;
  PostRepository postRepository;
  RedisService redisService;
  Publisher publisher;

  @Transactional
  @RabbitListener(queues = RabbitMQConfig.UPLOAD_QUEUE_NAME)
  public void uploadImageConsumer(Base64UploadDto message) {
    Result result = imageKitService.uploadFromBase64(message);
    log.info("Tải lên hình ảnh thành công: {}", result.getUrl());

    String fileId = result.getFileId();
    String url = result.getUrl();
    ImageEntity image = imageRepository.findById(message.getImageId())
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy hình ảnh"));
    image.setFileId(fileId);
    image.setUrl(url);
    imageRepository.save(image);
    log.info("Cập nhật hình ảnh có fileId: {} và url: {} thành công", fileId, url);

    PostEntity post = postRepository.findByIdForUpdate(message.getPostId())
        .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy bài viết"));

    String base64RedisKey = redisService.setKey(message.getImageId(), ":image:");
    String base64Src = redisService.getString(base64RedisKey);
    if (base64Src == null) {
      log.warn("Không tìm thấy src gốc trong Redis cho imageId {}", message.getImageId());
      return;
    }

    Document doc = Jsoup.parse(post.getContent());
    Elements imgTags = doc.select("img[src]");
    for (Element img : imgTags) {
      if (img.attr("src").equals(base64Src)) {
        img.attr("src", url);
      }
    }
    post.setContent(doc.body().html());
    postRepository.save(post);

    log.info("Đã thay src ảnh {} thành {}", message.getImageId(), url);

    String qImgRedisKey = redisService.setKey(message.getPostId(), ":image:");
    int qImgPending = redisService.decrement(qImgRedisKey);

    log.info("Ảnh {} của post {} đã upload xong ({}/{})", message.getImageId(), message.getPostId(), qImgPending,
        message.getTotalImages());

    if (qImgPending == 0) {
      ImageUploadedDto uploadedMess = ImageUploadedDto.builder().service("post").userId(message.getUserId()).build();
      publisher.sendUploadedAllImages(uploadedMess);
      redisService.deleteString(qImgRedisKey);
    }

    redisService.deleteString(base64RedisKey);
  }

  @RabbitListener(queues = RabbitMQConfig.DELETE_QUEUE_NAME)
  public void deleteImageConsumer(String fileId) {
    imageKitService.deleteImage(fileId);
    log.info("Xóa hình ảnh có fileId: {} thành công", fileId);
  }
}
