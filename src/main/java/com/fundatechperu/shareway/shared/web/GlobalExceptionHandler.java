package com.fundatechperu.shareway.shared.web;

import com.fundatechperu.shareway.shared.exception.ConflictException;
import com.fundatechperu.shareway.shared.exception.DomainValidationException;
import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import com.fundatechperu.shareway.shared.exception.ServiceUnavailableException;
import com.fundatechperu.shareway.shared.web.ErrorResponse.FieldErrorDetail;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates exceptions into the uniform {@link ErrorResponse} body for every bounded context.
 *
 * <p>Bounded contexts throw the shared exceptions of {@code shared.exception} (or subclasses of
 * them); a context with a status of its own declares a more specific advice with higher priority.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String VALIDATION_FAILED = "Validation failed";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleInvalidBody(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<FieldErrorDetail> fieldErrors = new ArrayList<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            error ->
                fieldErrors.add(new FieldErrorDetail(error.getField(), error.getDefaultMessage())));
    ex.getBindingResult()
        .getGlobalErrors()
        .forEach(
            error ->
                fieldErrors.add(
                    new FieldErrorDetail(error.getObjectName(), error.getDefaultMessage())));
    return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, request, fieldErrors);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleInvalidParameters(
      HandlerMethodValidationException ex, HttpServletRequest request) {
    List<FieldErrorDetail> fieldErrors =
        ex.getParameterValidationResults().stream()
            .flatMap(
                result ->
                    result.getResolvableErrors().stream()
                        .map(
                            error ->
                                new FieldErrorDetail(
                                    result.getMethodParameter().getParameterName(),
                                    error.getDefaultMessage())))
            .toList();
    return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, request, fieldErrors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    List<FieldErrorDetail> fieldErrors =
        ex.getConstraintViolations().stream()
            .map(
                violation ->
                    new FieldErrorDetail(
                        violation.getPropertyPath().toString(), violation.getMessage()))
            .toList();
    return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, request, fieldErrors);
  }

  @ExceptionHandler(DomainValidationException.class)
  public ResponseEntity<ErrorResponse> handleDomainValidation(
      DomainValidationException ex, HttpServletRequest request) {
    List<FieldErrorDetail> fieldErrors =
        ex.getField()
            .map(field -> List.of(new FieldErrorDetail(field, ex.getMessage())))
            .orElse(List.of());
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, fieldErrors);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableBody(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    return build(
        HttpStatus.BAD_REQUEST,
        "Malformed request body or invalid field value",
        request,
        List.of());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    String message = "Invalid value '%s' for parameter '%s'".formatted(ex.getValue(), ex.getName());
    return build(
        HttpStatus.BAD_REQUEST,
        message,
        request,
        List.of(new FieldErrorDetail(ex.getName(), message)));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParameter(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    String message = "Required parameter '%s' is missing".formatted(ex.getParameterName());
    return build(
        HttpStatus.BAD_REQUEST,
        message,
        request,
        List.of(new FieldErrorDetail(ex.getParameterName(), message)));
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ErrorResponse> handleMissingPart(
      MissingServletRequestPartException ex, HttpServletRequest request) {
    String message = "Required part '%s' is missing".formatted(ex.getRequestPartName());
    return build(
        HttpStatus.BAD_REQUEST,
        message,
        request,
        List.of(new FieldErrorDetail(ex.getRequestPartName(), message)));
  }

  @ExceptionHandler(ServletRequestBindingException.class)
  public ResponseEntity<ErrorResponse> handleBinding(
      ServletRequestBindingException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxUploadSize(
      MaxUploadSizeExceededException ex, HttpServletRequest request) {
    String message = "File exceeds the maximum allowed size";
    return build(
        HttpStatus.BAD_REQUEST, message, request, List.of(new FieldErrorDetail("file", message)));
  }

  @ExceptionHandler(MultipartException.class)
  public ResponseEntity<ErrorResponse> handleMultipart(
      MultipartException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "Invalid multipart request", request, List.of());
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthentication(
      AuthenticationException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest request) {
    return build(
        HttpStatus.FORBIDDEN,
        "You do not have permission to access this resource",
        request,
        List.of());
  }

  @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFound(
      RuntimeException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResource(
      NoResourceFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, "Resource not found", request, List.of());
  }

  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleServiceUnavailable(
      ServiceUnavailableException ex, HttpServletRequest request) {
    return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(
      ConflictException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler({
    ObjectOptimisticLockingFailureException.class,
    PessimisticLockingFailureException.class
  })
  public ResponseEntity<ErrorResponse> handleConcurrentUpdate(
      RuntimeException ex, HttpServletRequest request) {
    return build(
        HttpStatus.CONFLICT,
        "The resource was modified concurrently, try again",
        request,
        List.of());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrity(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    log.warn(
        "Data integrity violation on {}: {}",
        request.getRequestURI(),
        ex.getMostSpecificCause().getMessage());
    return build(
        HttpStatus.CONFLICT, "The request conflicts with existing data", request, List.of());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
    return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
    log.error("Unexpected error on {}", request.getRequestURI(), ex);
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, List.of());
  }

  private ResponseEntity<ErrorResponse> build(
      HttpStatus status,
      String message,
      HttpServletRequest request,
      List<FieldErrorDetail> fieldErrors) {
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(status, message, request.getRequestURI(), fieldErrors));
  }
}
