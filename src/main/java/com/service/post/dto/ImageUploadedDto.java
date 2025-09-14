package com.service.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ImageUploadedDto {
  private String service;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("post_id")
  private String postId;
}
