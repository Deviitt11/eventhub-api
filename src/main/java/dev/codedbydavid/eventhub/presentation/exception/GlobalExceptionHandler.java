package dev.codedbydavid.eventhub.presentation.exception;

import dev.codedbydavid.eventhub.domain.event.EventNotFoundException;
import dev.codedbydavid.eventhub.domain.event.EventValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {
                String details = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                ErrorResponse errorResponse = new ErrorResponse(
                                "VALIDATION_ERROR",
                                "Validation failed",
                                details,
                                Instant.now(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(EventValidationException.class)
        public ResponseEntity<ErrorResponse> handleEventValidationException(
                        EventValidationException ex, HttpServletRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                "DOMAIN_VALIDATION_ERROR",
                                "Event validation failed",
                                ex.getMessage(),
                                Instant.now(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(EventNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleEventNotFoundException(
                        EventNotFoundException ex, HttpServletRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                "NOT_FOUND",
                                "Event not found",
                                ex.getMessage(),
                                Instant.now(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolationException(
                        ConstraintViolationException ex, HttpServletRequest request) {
                String details = ex.getConstraintViolations().stream()
                                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                                .collect(Collectors.joining(", "));

                ErrorResponse errorResponse = new ErrorResponse(
                                "VALIDATION_ERROR",
                                "Constraint validation failed",
                                details,
                                Instant.now(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException ex, HttpServletRequest request) {
                String details = ex.getMessage();
                if (details != null && details.contains("JSON parse error")) {
                        details = "Invalid JSON format or date format. Expected ISO-8601 format (e.g., 2026-01-26T19:46:49.544Z)";
                }

                ErrorResponse errorResponse = new ErrorResponse(
                                "INVALID_REQUEST",
                                "Request body is invalid or malformed",
                                details,
                                Instant.now(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                Exception ex, HttpServletRequest request) {

                log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);

                ErrorResponse errorResponse = new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred",
                        null,
                        Instant.now(),
                        request.getRequestURI()
                );

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
}
