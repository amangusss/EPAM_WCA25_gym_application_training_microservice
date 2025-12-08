package com.github.amangusss.controller;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.service.TrainerWorkloadService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workload")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrainerWorkloadController {

    TrainerWorkloadService service;

    @PostMapping
    public ResponseEntity<Void> obtainWorkload(
            @Valid @RequestBody TrainerWorkloadDTO.Request.Create request,
            @RequestHeader(value = "X-transaction-id", required = false) String transactionId) {
        String transactionIdToUse = resolveTransactionId(transactionId);

        log.info("[{}] POST /api/v1/workload | Request: username={}, action={}, date={}, duration={}",
                transactionIdToUse,
                request.username(),
                request. actionType(),
                request.trainingDate(),
                request. trainingDuration());

        service.obtainWorkload(request, transactionIdToUse);

        log.info("[{}] POST /api/v1/workload | Response: OK", transactionIdToUse);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadDTO.Response.Summary> getTrainerSummary(
            @PathVariable String username,
            @RequestHeader(value = "X-transaction-id", required = false) String transactionId) {
        String transactionIdToUse = resolveTransactionId(transactionId);

        log.info("[{}] GET /api/v1/workload/username | Request: username={}", transactionIdToUse, username);

        TrainerWorkloadDTO.Response.Summary response = service.getTrainerSummary(username, transactionIdToUse);

        log.info("[{}] GET /api/v1/workload/username | Response: Summary retrieved for username={}",
                transactionIdToUse,
                username);

        return ResponseEntity.ok(response);
    }

    private String resolveTransactionId(String transactionId) {
        return transactionId != null ? transactionId : UUID.randomUUID().toString();
    }
}
