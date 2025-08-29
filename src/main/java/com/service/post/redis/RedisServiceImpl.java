package com.service.post.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.service.post.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {
  private final StringRedisTemplate stringRedisTemplate;
  private static final String serviceName = "post-service";

  @Override
  public String setKey(String originKey, String keyType) {
    return new StringBuilder().append(serviceName).append(keyType).append(originKey).toString();
  }

  @Override
  public void saveString(String key, String content, long timeout, TimeUnit unit) {
    stringRedisTemplate.opsForValue().set(key, content, timeout, unit);
    log.info("Đã lưu key '{}' vào Redis với TTL {} {}", key, timeout, unit);
  }

  @Override
  public String getString(String key) {
    if (!checkKeyExists(key, true)) {
      throw new ResourceNotFoundException("Không tìm thấy dữ liệu trong bộ nhớ");
    }
    return stringRedisTemplate.opsForValue().get(key);
  }

  @Override
  public void updateString(String key, String content) {
    if (!checkKeyExists(key, true)) {
      throw new ResourceNotFoundException("Không tìm thấy dữ liệu trong bộ nhớ");
    }
    stringRedisTemplate.opsForValue().set(key, content);
  }

  @Override
  public void deleteString(String key) {
    if (!checkKeyExists(key, true)) {
      throw new ResourceNotFoundException("Không tìm thấy dữ liệu trong bộ nhớ");
    }
    stringRedisTemplate.delete(key);
  }

  private boolean checkKeyExists(String key, boolean isString) {
    return stringRedisTemplate.hasKey(key);
  }
}
