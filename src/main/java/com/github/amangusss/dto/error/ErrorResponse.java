package com.github.amangusss.dto.error;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
    String transactionId,
    String error,
    String message,
    String path,
    String timestamp,
    int status
) {
    public ErrorResponse(String transactionId, String error, String message, String path, String timestamp, HttpStatus status) {
        this(transactionId, error, message, path, timestamp, status.value());
    }
}
