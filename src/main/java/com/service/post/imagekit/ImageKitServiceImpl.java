package com.service.post.imagekit;

import org.springframework.stereotype.Service;

import com.service.post.dto.Base64UploadDto;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageKitServiceImpl implements ImageKitService {
  private final ImageKit imageKit;

  @Override
  public Result uploadFromBase64(Base64UploadDto dto) {
    try {
      FileCreateRequest request = new FileCreateRequest(dto.getBase64Data(), dto.getFileName());
      request.setFolder(dto.getFolder());
      return imageKit.upload(request);
    } catch (Exception e) {
      throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage());
    }
  }

  @Override
  public void deleteImage(String fileId) {
    try {
      imageKit.deleteFile(fileId);
    } catch (Exception e) {
      throw new RuntimeException("Xóa ảnh thất bại: " + e.getMessage());
    }
  }
}
