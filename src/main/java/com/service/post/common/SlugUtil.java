package com.service.post.common;

import com.github.slugify.Slugify;

public class SlugUtil {
  private static final Slugify slugify = Slugify.builder().build();

  public static String toSlug(String str) {
    return slugify.slugify(str);
  }
}
