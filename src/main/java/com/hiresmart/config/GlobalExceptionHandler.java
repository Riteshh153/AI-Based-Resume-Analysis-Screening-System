package com.hiresmart.config;

import com.hiresmart.dto.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception ex) {
        log.error("REST Exception intercepted: {}", ex.getMessage(), ex);
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "An error occurred while processing your request.";
        }
        return ResponseEntity.ok(AuthResponse.failure(msg));
    }
}
