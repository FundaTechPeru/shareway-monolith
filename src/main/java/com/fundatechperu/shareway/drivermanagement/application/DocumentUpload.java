package com.fundatechperu.shareway.drivermanagement.application;

import com.fundatechperu.shareway.drivermanagement.domain.model.DocumentType;

public record DocumentUpload(
    DocumentType type, String originalFilename, String contentType, byte[] content) {}
