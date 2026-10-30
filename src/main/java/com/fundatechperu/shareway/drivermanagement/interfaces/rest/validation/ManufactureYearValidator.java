package com.fundatechperu.shareway.drivermanagement.interfaces.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Year;

public class ManufactureYearValidator implements ConstraintValidator<ManufactureYear, Integer> {

  private int min;

  @Override
  public void initialize(ManufactureYear annotation) {
    this.min = annotation.min();
  }

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    int max = Year.now().getValue() + 1;
    if (value >= min && value <= max) {
      return true;
    }
    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate("must be between %d and %d".formatted(min, max))
        .addConstraintViolation();
    return false;
  }
}
