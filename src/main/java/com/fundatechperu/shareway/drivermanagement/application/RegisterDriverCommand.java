package com.fundatechperu.shareway.drivermanagement.application;

public record RegisterDriverCommand(
    String licenseNumber,
    String emergencyContactName,
    String emergencyContactPhone,
    String emergencyContactRelationship) {}
