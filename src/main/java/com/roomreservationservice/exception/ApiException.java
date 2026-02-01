package com.roomreservationservice.exception;


import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiException(
        HttpStatus status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {
    public ApiException(HttpStatus status, String message) {
        this(status, message, LocalDateTime.now(), null);
    }

    public ApiException(HttpStatus status, Map<String, String> validationErrors) {
        this(status, "Validation failed", LocalDateTime.now(), validationErrors);
    }
}