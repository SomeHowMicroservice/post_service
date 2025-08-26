package com.service.post.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.imagekit.sdk.ImageKit;

@Configuration
public class ImageKitConfig {
  @Value("${spring.imagekit.public_key}")
  private String publicKey;

  @Value("${spring.imagekit.private_key}")
  private String privateKey;

  @Value("${spring.imagekit.url_endpoint}")
  private String urlEndpoint;

  @Bean
  public ImageKit imageKit() {
    io.imagekit.sdk.config.Configuration config = new io.imagekit.sdk.config.Configuration(publicKey, privateKey,
        urlEndpoint);
    ImageKit imageKit = ImageKit.getInstance();
    imageKit.setConfig(config);
    return imageKit;
  }
}
