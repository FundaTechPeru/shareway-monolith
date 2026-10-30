package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmergencyContactRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank
        @Pattern(
            regexp = "^\\+?\\d{9,15}$",
            message = "must have 9 to 15 digits with an optional leading +")
        String phone,
    @NotBlank @Size(max = 50) String relationship) {}
