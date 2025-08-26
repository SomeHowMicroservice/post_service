package com.service.post.grpc_clients;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GrpcClientFactory {
  private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();

  public <T> T getStub(String target, Function<ManagedChannel, T> stubFn) {
    ManagedChannel channel = channels.computeIfAbsent(target, t -> {
      log.info("Đang tạo kênh gRPC tới {}", t);
      return ManagedChannelBuilder.forTarget(t).usePlaintext().enableRetry().maxRetryAttempts(3)
          .idleTimeout(5, TimeUnit.MINUTES).build();
    });
    return stubFn.apply(channel);
  }

  @PreDestroy
  public void shutdown() {
    channels.forEach((t, ch) -> {
      log.info("Tắt kết nối tới kênh {}", t);
      ch.shutdown();
    });
  }
}
