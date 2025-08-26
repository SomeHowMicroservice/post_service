package com.service.post.mq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.service.post.config.RabbitMQConfig;
import com.service.post.dto.Base64UploadDto;
import com.service.post.entity.ImageEntity;
import com.service.post.exceptions.ResourceNotFoundException;
import com.service.post.imagekit.ImageKitService;
import com.service.post.repository.ImageRepository;

import io.imagekit.sdk.models.results.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class Consumer {
  private final ImageKitService imageKitService;
  private final ImageRepository imageRepository;

  @RabbitListener(queues = RabbitMQConfig.UPLOAD_QUEUE_NAME)
  public void uploadImageConsumer(Base64UploadDto message) {
    Result result = imageKitService.uploadFromBase64(message);
    log.info("Tải lên hình ảnh thành công: {}", result.getUrl());

    String fileId = result.getFileId();
    ImageEntity image = imageRepository.findById(message.getImageId()).orElseThrow(() -> new ResourceNotFoundException("không tìm thấy hình ảnh"));
    image.setFileId(fileId);
    imageRepository.save(image);
    log.info("Cập nhật hình ảnh có fileId: {} thành công", fileId);
  }
}
