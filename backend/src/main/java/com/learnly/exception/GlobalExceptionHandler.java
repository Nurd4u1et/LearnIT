package com.learnly.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handle(ApiException ex) {
        return ResponseEntity.status(ex.status()).body(Map.of(
                "error", ex.status().getReasonPhrase(),
                "message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handle(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(fe -> fields.put(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Validation failed",
                "fields", fields));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handle(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal Server Error",
                "message", ex.getMessage() == null ? "Unexpected error" : ex.getMessage()));
    }
}
