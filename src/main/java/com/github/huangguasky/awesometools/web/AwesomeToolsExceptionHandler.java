package com.github.huangguasky.awesometools.web;

import com.github.huangguasky.awesometools.exception.AwesomeToolsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AwesomeToolsExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AwesomeToolsExceptionHandler.class);

    private final ErrorResponseBuilder errorResponseBuilder;

    public AwesomeToolsExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        this.errorResponseBuilder = errorResponseBuilder;
    }

    @ExceptionHandler(AwesomeToolsException.class)
    public ResponseEntity<ApiErrorResponse> handleAwesomeToolsException(
            AwesomeToolsException ex, HttpServletRequest request) {
        return response("AWESOME_TOOLS_ERROR", ex.getMessage(), HttpStatus.BAD_REQUEST, request, Map.of());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiErrorResponse> handleBindException(Exception ex, HttpServletRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        if (ex instanceof MethodArgumentNotValidException validException) {
            validException.getBindingResult().getFieldErrors()
                    .forEach(error -> details.put(error.getField(), error.getDefaultMessage()));
        } else if (ex instanceof BindException bindException) {
            bindException.getBindingResult().getFieldErrors()
                    .forEach(error -> details.put(error.getField(), error.getDefaultMessage()));
        }
        return response("VALIDATION_ERROR", "Request validation failed", HttpStatus.BAD_REQUEST, request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                details.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return response("VALIDATION_ERROR", "Request validation failed", HttpStatus.BAD_REQUEST, request, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return response("INTERNAL_ERROR", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> response(String code, String message, HttpStatus status,
            HttpServletRequest request, Map<String, String> details) {
        return ResponseEntity.status(status)
                .body(errorResponseBuilder.build(code, message, status, request, details));
    }
}
