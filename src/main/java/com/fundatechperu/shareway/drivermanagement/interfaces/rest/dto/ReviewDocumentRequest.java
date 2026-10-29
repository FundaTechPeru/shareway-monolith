package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fundatechperu.shareway.drivermanagement.application.ReviewDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewDocumentRequest(
    @NotNull ReviewDecision decision, @Size(max = 500) String reason) {}
