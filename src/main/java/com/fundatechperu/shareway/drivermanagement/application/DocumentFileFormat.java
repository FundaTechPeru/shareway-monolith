package com.fundatechperu.shareway.drivermanagement.application;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * File formats accepted for driver documents. A file is accepted only when its declared content
 * type is allowed and its first bytes match that format's signature.
 */
enum DocumentFileFormat {
  PDF("application/pdf", ".pdf", new byte[] {0x25, 0x50, 0x44, 0x46}),
  JPEG("image/jpeg", ".jpg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
  PNG("image/png", ".png", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

  private final String contentType;
  private final String extension;
  private final byte[] signature;

  DocumentFileFormat(String contentType, String extension, byte[] signature) {
    this.contentType = contentType;
    this.extension = extension;
    this.signature = signature;
  }

  static Optional<DocumentFileFormat> detect(String declaredContentType, byte[] content) {
    if (declaredContentType == null) {
      return Optional.empty();
    }
    String normalized = declaredContentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
    return Arrays.stream(values())
        .filter(format -> format.contentType.equals(normalized))
        .filter(format -> format.matches(content))
        .findFirst();
  }

  String contentType() {
    return contentType;
  }

  String extension() {
    return extension;
  }

  private boolean matches(byte[] content) {
    return content.length >= signature.length
        && Arrays.equals(content, 0, signature.length, signature, 0, signature.length);
  }
}
