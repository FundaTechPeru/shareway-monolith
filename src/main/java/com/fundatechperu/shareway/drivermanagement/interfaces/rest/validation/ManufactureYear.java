package com.fundatechperu.shareway.drivermanagement.interfaces.rest.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** The annotated year must be between {@link #min()} and the current year plus one. */
@Documented
@Constraint(validatedBy = ManufactureYearValidator.class)
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface ManufactureYear {

  String message() default "must be between {min} and next year";

  int min() default 2000;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
