package com.fundatechperu.shareway.drivermanagement.domain.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum DocumentType {
  DRIVERS_LICENSE(true),
  NATIONAL_ID(true),
  CRIMINAL_RECORD(true),
  VEHICLE_REGISTRATION(false);

  private final boolean requiredForVerification;

  DocumentType(boolean requiredForVerification) {
    this.requiredForVerification = requiredForVerification;
  }

  public boolean isRequiredForVerification() {
    return requiredForVerification;
  }

  public static Set<DocumentType> requiredForVerification() {
    return Arrays.stream(values())
        .filter(DocumentType::isRequiredForVerification)
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(DocumentType.class)));
  }
}
