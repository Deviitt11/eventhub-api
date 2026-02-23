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
                Throwable root = ex.getMostSpecificCause();

                String sqlState = null;
                String constraint = null;

                // Extract SQLState + constraint name in a vendor-agnostic way
                if (root instanceof org.hibernate.exception.ConstraintViolationException cve) {
                        SQLException sqlException = cve.getSQLException();
                        if (sqlException != null) {
                                sqlState = sqlException.getSQLState();
                        }
                        constraint = cve.getConstraintName();
                } else if (root instanceof SQLException sqlException) {
                        sqlState = sqlException.getSQLState();
                }

                // Postgres SQLState reference (common ones):
                // 23505 = unique_violation
                // 23514 = check_violation
                // 23502 = not_null_violation
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
                        status = HttpStatus.BAD_REQUEST;
                        code = "CONSTRAINT_VIOLATION";
                        message = "Database constraint violation";
                        details = "A check constraint was violated";
                } else if ("23502".equals(sqlState)) {
                        status = HttpStatus.BAD_REQUEST;
                        code = "CONSTRAINT_VIOLATION";
                        message = "Database constraint violation";
                        details = "A required database field was null";
                }

                log.warn("Data integrity violation for {} {} (sqlState={}, constraint={}): {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        sqlState,
                        constraint,
                        root != null ? root.getMessage() : ex.getMessage()
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
