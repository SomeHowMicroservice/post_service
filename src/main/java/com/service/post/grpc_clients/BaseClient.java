package com.service.post.grpc_clients;

import java.util.function.Function;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BaseClient<Stub> {
  final GrpcClientFactory factory;
  Stub stub;

  <Req, Res> Res call(Function<Stub, Res> fn, long timeoutSec) {
    try {
      return fn.apply(stub);
    } catch (StatusRuntimeException e) {
      throw e;
    }
  }
}
