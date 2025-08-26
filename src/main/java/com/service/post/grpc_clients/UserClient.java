package com.service.post.grpc_clients;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.service.user.GetUserByIdRequest;
import com.service.user.GetUsersByIdRequest;
import com.service.user.UserResponse;
import com.service.user.UserServiceGrpc;
import com.service.user.UsersPublicResponse;

import jakarta.annotation.PostConstruct;

@Component
public class UserClient extends BaseClient<UserServiceGrpc.UserServiceBlockingStub> {
  public UserClient(GrpcClientFactory factory) {
    super(factory);
  }

  @Value("${spring.grpc.server.host}")
  private String grpcHost;

  @Value("${spring.grpc.services.user.port}")
  private int userPort;

  private String target;

  @PostConstruct
  public void init() {
    target = grpcHost + ":" + userPort;
    stub = factory.getStub(target, UserServiceGrpc::newBlockingStub);
  }

  public UserResponse getUserById(String id) {
    return call(
        s -> s.withDeadlineAfter(2, TimeUnit.SECONDS).getUserById(GetUserByIdRequest.newBuilder().setId(id).build()),
        2);
  }

  public UsersPublicResponse getUsersById(List<String> ids) {
    GetUsersByIdRequest request = GetUsersByIdRequest.newBuilder().addAllIds(ids).build();
    return call(s -> s.withDeadlineAfter(3, TimeUnit.SECONDS).getUsersById(request), 3);
  }
}
