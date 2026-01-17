package com.github.amangusss.exception;

import com.github.amangusss.dto.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleTrainerNotFound(TrainerNotFoundException e, WebRequest request) {
        String transactionId = getTransactionId(request);

        log.error("[Transaction: {}] Trainer not found: {}", transactionId, e.getMessage());

        return buildResponse(HttpStatus.NOT_FOUND,
                "Trainer Not Found",
                e.getMessage(),
                transactionId,
                request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleWorkloadNotFound(WorkloadNotFoundException e, WebRequest request) {
        String transactionId = getTransactionId(request);

        log.error("[Transaction: {}] Workload not found: {}", transactionId, e.getMessage());

        return buildResponse(HttpStatus.NOT_FOUND,
                "Workload Not Found",
                e.getMessage(),
                transactionId,
                request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException e, WebRequest request) {
        String transactionId = getTransactionId(request);

        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.error("[Transaction: {}] Validation error: {}", transactionId, message);

        return buildResponse(HttpStatus.BAD_REQUEST,
                "Validation Error",
                message,
                transactionId,
                request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, WebRequest request) {
        String transactionId = getTransactionId(request);

        log.error("[Transaction: {}] Illegal argument: {}", transactionId, e.getMessage());

        return buildResponse(HttpStatus.BAD_REQUEST,
                "Bad Request",
                e.getMessage(),
                transactionId,
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e, WebRequest request) {
        String transactionId = getTransactionId(request);

        log.error("[Transaction: {}] Unexpected error: {}", transactionId, e.getMessage(), e);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred.",
                transactionId,
                request);
    }

    private String getTransactionId(WebRequest request) {
        String txId = request.getHeader("X-Transaction-Id");
        return txId != null ?  txId : UUID.randomUUID().toString();
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status,
                                                        String error,
                                                        String message,
                                                        String txId,
                                                        WebRequest request) {
        ErrorResponse response = new ErrorResponse(
                txId,
                error,
                message,
                getPath(request),
                Instant.now().toString(),
                status.value()
        );

        return ResponseEntity.status(status).body(response);
    }

}
