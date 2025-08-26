package com.service.post.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.service.post.config.RabbitMQConfig;
import com.service.post.dto.Base64UploadDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Publisher {
  private final RabbitTemplate rabbitTemplate;

  public void sendUploadImage(Base64UploadDto message) {
    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.UPLOAD_ROUTING_KEY, message);
  }
}
