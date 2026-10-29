package com.fundatechperu.shareway.drivermanagement.infrastructure.storage;

import com.fundatechperu.shareway.drivermanagement.domain.repository.DocumentStorage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DocumentStorageAdapter implements DocumentStorage {

  private static final Logger log = LoggerFactory.getLogger(DocumentStorageAdapter.class);

  private final Path root;

  public DocumentStorageAdapter(StorageProperties properties) {
    this.root = Path.of(properties.path()).toAbsolutePath().normalize();
  }

  @Override
  public String store(byte[] content, String extension) {
    String storageKey = UUID.randomUUID() + extension;
    try {
      Files.createDirectories(root);
      Files.write(resolve(storageKey), content, StandardOpenOption.CREATE_NEW);
    } catch (IOException ex) {
      throw new UncheckedIOException("Could not store document", ex);
    }
    return storageKey;
  }

  @Override
  public void delete(String storageKey) {
    try {
      Files.deleteIfExists(resolve(storageKey));
    } catch (IOException ex) {
      log.warn("Could not delete stored document {}", storageKey, ex);
    }
  }

  private Path resolve(String storageKey) {
    Path file = root.resolve(storageKey).normalize();
    if (!file.getParent().equals(root)) {
      throw new IllegalArgumentException("Invalid storage key: " + storageKey);
    }
    return file;
  }
}
