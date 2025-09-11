package com.service.post.grpc_clients;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.service.user.GetManyRequest;
import com.service.user.GetOneRequest;
import com.service.user.UserPublicResponse;
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

  public UserPublicResponse getUserPublicById(String id) {
    return (UserPublicResponse) call(s -> s.withDeadlineAfter(2, TimeUnit.SECONDS)
        .getUserPublicById(GetOneRequest.newBuilder().setId(id).build()), 2);
  }

  public UsersPublicResponse getUsersPublicById(List<String> ids) {
    GetManyRequest request = GetManyRequest.newBuilder().addAllIds(ids).build();
    return (UsersPublicResponse) call(s -> s.withDeadlineAfter(3, TimeUnit.SECONDS).getUsersPublicById(request), 3);
  }
}
