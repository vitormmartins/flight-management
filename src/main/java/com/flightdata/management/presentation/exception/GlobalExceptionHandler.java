package com.flightdata.management.presentation.exception;

import com.flightdata.management.application.dto.ErrorResponse;
import com.flightdata.management.domain.service.FlightDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Handles exceptions and converts them to appropriate HTTP responses.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-26
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handle flight didn't find exceptions.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with status 404
   */
  @ExceptionHandler(FlightDomainService.FlightNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleFlightNotFoundException(
          FlightDomainService.FlightNotFoundException ex,
          WebRequest request) {

    log.error("Flight not found: {}", ex.getMessage());

    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /**
   * Handles validation errors.
   *
   * @param ex the validation exception
   * @param request the web request
   * @return error response with status 400
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
          MethodArgumentNotValidException ex,
          WebRequest request) {

    String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

    log.error("Validation error: {}", errorMessage);

    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("Validation failed: " + errorMessage)
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handles illegal argument exceptions.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with status 400
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
          IllegalArgumentException ex,
          WebRequest request) {

    log.error("Illegal argument: {}", ex.getMessage());

    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handles illegal state exceptions.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with status 400
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(
          IllegalStateException ex,
          WebRequest request) {

    log.error("Illegal state: {}", ex.getMessage());

    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handles all other exceptions.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with status 500
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
          Exception ex,
          WebRequest request) {

    log.error("Unexpected error occurred", ex);

    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .message("An unexpected error occurred. Please try again later.")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}