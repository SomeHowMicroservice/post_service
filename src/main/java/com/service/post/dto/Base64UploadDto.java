package com.service.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Base64UploadDto {
  private String imageId;
  private String base64Data;
  private String fileName;
  private String folder;
}
