package com.service.post.controller;

import org.springframework.grpc.server.service.GrpcService;

import com.service.post.CreatePostRequest;
import com.service.post.CreateTopicRequest;
import com.service.post.CreatedResponse;
import com.service.post.DeleteManyRequest;
import com.service.post.DeleteOneRequest;
import com.service.post.DeletedResponse;
import com.service.post.GetManyRequest;
import com.service.post.PermanentlyDeleteManyRequest;
import com.service.post.PermanentlyDeleteOneRequest;
import com.service.post.PostServiceGrpc.PostServiceImplBase;
import com.service.post.RestoreManyRequest;
import com.service.post.RestoreOneRequest;
import com.service.post.RestoredResponse;
import com.service.post.TopicsAdminResponse;
import com.service.post.UpdateTopicRequest;
import com.service.post.UpdatedResponse;
import com.service.post.exceptions.AlreadyExistsException;
import com.service.post.exceptions.ResourceNotFoundException;
import com.service.post.service.PostService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@GrpcService
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GrpcController extends PostServiceImplBase {
  PostService postService;

  @Override
  public void createTopic(CreateTopicRequest request, StreamObserver<CreatedResponse> responseObserver) {
    try {
      String topicId = postService.createTopic(request);
      CreatedResponse response = CreatedResponse.newBuilder().setId(topicId).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (AlreadyExistsException e) {
      responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL.withDescription("tạo chủ đề bài viết thất bại: " + e.getMessage()).asRuntimeException());
    }
  }

  @Override
  public void getAllTopicsAdmin(GetManyRequest request, StreamObserver<TopicsAdminResponse> responseObserver) {
    try {
      TopicsAdminResponse convertedTopics = postService.getAllTopicsAdmin();
      responseObserver.onNext(convertedTopics);
      responseObserver.onCompleted();
      return;
    } catch (StatusRuntimeException e) {
      responseObserver.onError(e);
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL.withDescription("lấy danh sách chủ đề thất bại: " + e.getMessage()).asRuntimeException());
    }
  }

  public void getDeletedTopics(GetManyRequest request, StreamObserver<TopicsAdminResponse> responseObserver) {
    try {
      TopicsAdminResponse convertedTopics = postService.getDeletedTopics();
      responseObserver.onNext(convertedTopics);
      responseObserver.onCompleted();
      return;
    } catch (Exception e) {
      responseObserver.onError(Status.INTERNAL
          .withDescription("lấy danh sách chủ đề đã xóa thất bại: " + e.getMessage()).asRuntimeException());
    }
  }

  @Override
  public void updateTopic(UpdateTopicRequest request, StreamObserver<UpdatedResponse> responseObserver) {
    try {
      postService.updateTopic(request);
      UpdatedResponse response = UpdatedResponse.newBuilder().setSuccess(true).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (AlreadyExistsException e) {
      responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
    } catch (ResourceNotFoundException e) {
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL.withDescription("cập nhật chủ đề bài viết thất bại: " + e.getMessage()).asRuntimeException());
    }
  }

  @Override
  public void deleteTopic(DeleteOneRequest request, StreamObserver<DeletedResponse> responseObserver) {
    try {
      postService.deleteTopic(request);
      DeletedResponse response = DeletedResponse.newBuilder().setSuccess(true).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (ResourceNotFoundException e) {
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL.withDescription("chuyển chủ đề bài viết vào thùng rác thất bại: " + e.getMessage())
              .asRuntimeException());
    }
  }

  @Override
  public void deleteTopics(DeleteManyRequest request, StreamObserver<DeletedResponse> responseObserver) {
    try {
      postService.deleteTopics(request);
      DeletedResponse response = DeletedResponse.newBuilder().setSuccess(true).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (ResourceNotFoundException e) {
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL.withDescription("chuyển danh sách chủ đề vào thùng rác thất bại: " + e.getMessage())
              .asRuntimeException());
    }
  }

  @Override
  public void restoreTopic(RestoreOneRequest request, StreamObserver<RestoredResponse> responseObserver) {
    try {
      postService.restoreTopic(request);
      RestoredResponse response = RestoredResponse.newBuilder().setSuccess(true).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (ResourceNotFoundException e) {
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL.withDescription("khôi phục chủ đề bài viết thất bại: " + e.getMessage())
              .asRuntimeException());
    }
  }

  @Override
  public void restoreTopics(RestoreManyRequest request, StreamObserver<RestoredResponse> responseObserver) {
    try {
      postService.restoreTopics(request);
      RestoredResponse response = RestoredResponse.newBuilder().setSuccess(true).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (ResourceNotFoundException e) {
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL.withDescription("khôi phục danh sách chủ đề thất bại: " + e.getMessage())
              .asRuntimeException());
    }
  }

  @Override
  public void permanentlyDeleteTopic(PermanentlyDeleteOneRequest request,
      StreamObserver<DeletedResponse> responseObserver) {
    try {
      postService.permanentlyDeleteTopic(request.getId());
      DeletedResponse response = DeletedResponse.newBuilder().setSuccess(true).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (ResourceNotFoundException e) {
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      responseObserver.onError(Status.INTERNAL.withDescription("xóa chủ đề bài viết thất bại: " + e.getMessage())
          .asRuntimeException());
    }
  }

  @Override
  public void permanentlyDeleteTopics(PermanentlyDeleteManyRequest request,
      StreamObserver<DeletedResponse> responseObserver) {
    try {
      postService.permanentlyDeleteTopics(request.getIdsList());
      DeletedResponse response = DeletedResponse.newBuilder().setSuccess(true).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (ResourceNotFoundException e) {
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      responseObserver.onError(Status.INTERNAL.withDescription("xóa danh sách chủ đề thất bại: " + e.getMessage())
          .asRuntimeException());
    }
  }

  @Override
  public void createPost(CreatePostRequest request, StreamObserver<CreatedResponse> responseObserver) {
    try {
      String postId = postService.createPost(request);
      CreatedResponse response = CreatedResponse.newBuilder().setId(postId).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (ResourceNotFoundException e) {
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (AlreadyExistsException e) {
      responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      responseObserver
          .onError(Status.INTERNAL.withDescription("tạo bài viết thất bại: " + e.getMessage()).asRuntimeException());
    }
  }
}
