package com.service.post.redis;

import java.util.concurrent.TimeUnit;

public interface RedisService {
  String setKey(String originKey, String keyType);

  void saveString(String key, String content, long timeout, TimeUnit unit);

  String getString(String key);

  void updateString(String key, String content);

  void deleteString(String key);
}
