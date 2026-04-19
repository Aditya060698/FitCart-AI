package com.fitcart.api.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        List<String> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Constraint validation failed", details);
    }

    @ExceptionHandler(UpstreamServiceException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamService(UpstreamServiceException exception) {
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "AI_SERVICE_UNAVAILABLE",
                exception.getMessage() == null ? "Upstream AI service is unavailable" : exception.getMessage(),
                List.of("Retry the request or continue with the structured product experience.")
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception exception) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                exception.getMessage() == null ? "Unexpected server error" : exception.getMessage(),
                List.of()
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            List<String> details
    ) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(code, message, details, OffsetDateTime.now())
        );
    }
}
