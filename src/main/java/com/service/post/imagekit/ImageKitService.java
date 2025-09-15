package com.service.post.imagekit;

import com.service.post.dto.Base64UploadDto;
import com.service.post.dto.DeleteImageDto;

import io.imagekit.sdk.models.results.Result;

public interface ImageKitService {
  Result uploadFromBase64(Base64UploadDto dto);

  void deleteImage(DeleteImageDto dto);
}
