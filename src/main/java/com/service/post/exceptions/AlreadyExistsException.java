package com.service.post.exceptions;

public class AlreadyExistsException extends RuntimeException {
  public AlreadyExistsException(String mess) {
    super(mess);
  }
}
