package com.reliaquest.api.controller.config;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ControllerAdvice {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> handleFeignStatusException(FeignException e) {

        ErrorResponse feignException = new ErrorResponse(e.status(), e.getMessage(), e.contentUTF8());
        log.error(feignException.toString());

        return ResponseEntity
                .status(e.status())
                .body(e.contentUTF8());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> String.format("%s: %s", err.getField(), err.getDefaultMessage()))
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation error");

        ErrorResponse validationFailed = new ErrorResponse(400, "Validation failed", errors);
        log.error(validationFailed.toString());

        return ResponseEntity
                .badRequest()
                .body(validationFailed);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(HttpMessageNotReadableException ex) {

        ErrorResponse validationFailed = new ErrorResponse(400, "Validation failed", ex.getMessage());
        log.error(validationFailed.toString());

        return ResponseEntity
                .badRequest()
                .body(validationFailed);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolations(ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations()
                .stream()
                .map(cv -> String.format("%s: %s", cv.getPropertyPath(), cv.getMessage()))
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation error");

        ErrorResponse constraintViolation = new ErrorResponse(400, "Constraint violation", errors);
        log.error(constraintViolation.toString());

        return ResponseEntity
                .badRequest()
                .body(constraintViolation);
    }

    public record ErrorResponse(int status, String error, String message) {
    }
}
