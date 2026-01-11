package com.github.amangusss.controller;

import com.github.amangusss.dto.generated.TrainerSummaryResponse;
import com.github.amangusss.dto.generated.TrainingEventRequest;
import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.mapper.GeneratedDtoMapper;
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
    GeneratedDtoMapper generatedDtoMapper;

    @PostMapping
    public ResponseEntity<Void> processTraining(
            @Valid @RequestBody TrainingEventRequest request,
            @RequestHeader(value = "X-Transaction-Id", required = false) String transactionId) {
        String transactionIdToUse = resolveTransactionId(transactionId);

        log.info("[{}] POST /api/v1/workload | Request: username={}, action={}",
                transactionIdToUse, request.getUsername(), request.getActionType());

        TrainerWorkloadDTO.Request.Create internalDto = generatedDtoMapper.toInternalCreate(request);
        service.obtainWorkload(internalDto, transactionIdToUse);

        log.info("[{}] POST /api/v1/workload | Response: 200 OK", transactionIdToUse);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerSummaryResponse> getTrainerSummary(
            @PathVariable String username,
            @RequestHeader(value = "X-Transaction-Id", required = false) String transactionId) {
        String transactionIdToUse = resolveTransactionId(transactionId);

        log.info("[{}] GET /api/v1/workload/{} | Request: username={}", transactionIdToUse, username, username);

        TrainerWorkloadDTO.Response.Summary internalSummary = service.getTrainerSummary(username, transactionIdToUse);
        TrainerSummaryResponse response = generatedDtoMapper.toGeneratedSummary(internalSummary);

        log.info("[{}] GET /api/v1/workload/{} | Response: Summary retrieved for username={}",
                transactionIdToUse, username, username);

        return ResponseEntity.ok(response);
    }

    private String resolveTransactionId(String transactionId) {
        return transactionId != null ? transactionId : UUID.randomUUID().toString();
    }
}
