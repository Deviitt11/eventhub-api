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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import org.springframework.dao.DataIntegrityViolationException;

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

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
                        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
                String parameterName = ex.getName();
                Object rejectedValue = ex.getValue();
                String details = buildTypeMismatchDetails(parameterName, rejectedValue, ex.getRequiredType());

                log.warn("Invalid request parameter for {} {}: {}={}",
                                request.getMethod(),
                                request.getRequestURI(),
                                parameterName,
                                rejectedValue);

                ErrorResponse errorResponse = new ErrorResponse(
                                "VALIDATION_ERROR",
                                "Validation failed",
                                details,
                                Instant.now(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        private String buildTypeMismatchDetails(String parameterName, Object rejectedValue, Class<?> requiredType) {
                if (requiredType != null) {
                        if (TemporalAccessor.class.isAssignableFrom(requiredType)) {
                                return "%s: invalid value '%s'. Expected ISO-8601 date-time (e.g., 2030-01-01T10:00:00Z)"
                                                .formatted(parameterName, rejectedValue);
                        }

                        if (UUID.class.isAssignableFrom(requiredType)) {
                                return "%s: invalid value '%s'. Expected UUID".formatted(parameterName, rejectedValue);
                        }

                        return "%s: invalid value '%s'. Expected %s"
                                        .formatted(parameterName, rejectedValue, requiredType.getSimpleName());
                }

                return "%s: invalid value '%s'".formatted(parameterName, rejectedValue);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
                HttpMessageNotReadableException ex,
                HttpServletRequest request
        ) {
                // Client-safe message (no internals)
                String details = "Invalid JSON format or date format. Expected ISO-8601 format (e.g., 2026-01-26T19:46:49.544Z)";

                // Internal-only detail for debugging (includes parsing hints, stack causes, etc.)
                Throwable rootCause = ex.getMostSpecificCause();
                String internalMessage = rootCause.getMessage();

                if (internalMessage == null || internalMessage.isBlank()) {
                        internalMessage = rootCause.getClass().getSimpleName();
                }

                log.warn("Invalid request body for {} {}: {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        internalMessage);

                ErrorResponse errorResponse = new ErrorResponse(
                        "INVALID_REQUEST",
                        "Request body is invalid or malformed",
                        details,
                        Instant.now(),
                        request.getRequestURI()
                );

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Specific handler (409/400)
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
                DataIntegrityViolationException ex,
                HttpServletRequest request
        ) {
                String sqlState = null;
                String constraint = null;

                Throwable cursor = ex;
                while (cursor != null) {
                        if (constraint == null && cursor instanceof org.hibernate.exception.ConstraintViolationException cve) {
                                constraint = cve.getConstraintName();
                        }
                        if (sqlState == null && cursor instanceof SQLException sqlException) {
                                sqlState = sqlException.getSQLState();
                        }
                        if (sqlState != null && constraint != null) {
                                break;
                        }
                        cursor = cursor.getCause();
                }

                HttpStatus status = HttpStatus.BAD_REQUEST;
                String code = "CONSTRAINT_VIOLATION";
                String message = "Database constraint violation";
                String details = "Request violates a database constraint";

                if ("23505".equals(sqlState)) {
                        status = HttpStatus.CONFLICT;
                        code = "CONFLICT";
                        message = "Resource conflict";

                        if ("uq_events_title_starts_at".equals(constraint)) {
                                details = "An event with the same title and startsAt already exists";
                        } else {
                                details = "A unique constraint was violated";
                        }
                } else if ("23514".equals(sqlState)) {
                        details = "A check constraint was violated";
                } else if ("23502".equals(sqlState)) {
                        details = "A required database field was null";
                }

                Throwable mostSpecific = ex.getMostSpecificCause();
                log.warn("Data integrity violation for {} {} (sqlState={}, constraint={}): {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        sqlState,
                        constraint,
                        mostSpecific != null ? mostSpecific.getMessage() : ex.getMessage()
                );

                ErrorResponse errorResponse = new ErrorResponse(
                        code,
                        message,
                        details,
                        Instant.now(),
                        request.getRequestURI()
                );

                return ResponseEntity.status(status).body(errorResponse);
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
