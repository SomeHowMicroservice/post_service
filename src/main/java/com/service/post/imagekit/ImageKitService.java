package com.service.post.imagekit;

import com.service.post.dto.Base64UploadDto;

import io.imagekit.sdk.models.results.Result;

public interface ImageKitService {
  Result uploadFromBase64(Base64UploadDto request);

  void deleteImage(String fileId);
}
