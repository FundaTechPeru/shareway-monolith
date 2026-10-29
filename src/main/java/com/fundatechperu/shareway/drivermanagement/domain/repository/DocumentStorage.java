package com.fundatechperu.shareway.drivermanagement.domain.repository;

public interface DocumentStorage {

  /** Stores the content under a new unique key and returns that key. */
  String store(byte[] content, String extension);

  void delete(String storageKey);
}
