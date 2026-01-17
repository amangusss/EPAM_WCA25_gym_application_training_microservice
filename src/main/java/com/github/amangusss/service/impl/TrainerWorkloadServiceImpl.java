package com.github.amangusss.service.impl;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.TrainerWorkload;
import com.github.amangusss.exception.TrainerNotFoundException;
import com.github.amangusss.exception.WorkloadNotFoundException;
import com.github.amangusss.mapper.TrainerWorkloadMapper;
import com.github.amangusss.repository.TrainerWorkloadRepository;
import com.github.amangusss.service.TrainerWorkloadService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    TrainerWorkloadRepository repository;
    TrainerWorkloadMapper mapper;

    @Override
    public void obtainWorkload(TrainerWorkloadDTO.Request.Create request, String transactionId) {
        log.info("[{}][Transaction] Processing training event for trainer: {}, action: {}",
                transactionId, request.username(), request.actionType());

        YearMonth period = YearMonth.from(request.trainingDate());
        log.debug("[{}][Operation] Calculated period {} for training date {}",
                transactionId, period, request.trainingDate());

        switch (request.actionType()) {
            case ADD -> addTrainingHours(request, period, transactionId);
            case DELETE -> deleteTrainingHours(request.username(), period, request.trainingDuration(), transactionId);
        }

        log.info("[{}][Transaction] Completed processing training event for trainer: {}",
                transactionId, request.username());
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerWorkloadDTO.Response.Summary getTrainerSummary(String username, String transactionId) {
        log.info("[{}][Transaction] Retrieving workload summary for trainer {}", transactionId, username);

        List<TrainerWorkload> workloads = repository.findByUsername(username);
        log.debug("[{}][Operation] Found {} workload records for trainer {}",
                transactionId, workloads.size(), username);

        if (workloads.isEmpty()) {
            log.warn("[{}] Workload not found for trainer {}", transactionId, username);
            throw new TrainerNotFoundException(username);
        }

        TrainerWorkloadDTO.Response.Summary summary = mapper.toSummary(workloads);
        log.info("[{}][Transaction] Successfully retrieved summary for trainer: {}", transactionId, username);

        return summary;
    }

    private void addTrainingHours(TrainerWorkloadDTO.Request.Create request, YearMonth period, String transactionId) {
        log.debug("[{}][Operation] Searching for existing workload: username={}, period={}",
                transactionId, request.username(), period);

        Optional<TrainerWorkload> workloadOpt = repository.findByUsernameAndPeriod(request.username(), period);

        if (workloadOpt.isEmpty()) {
            log.debug("[{}][Operation] Creating new workload for trainer: {}, period: {}",
                    transactionId, request.username(), period);
            createNewWorkload(request, period, transactionId);
        } else {
            log.debug("[{}][Operation] Updating existing workload for trainer: {}, period: {}",
                    transactionId, request.username(), period);
            updateExistingWorkload(workloadOpt.get(), request, transactionId);
        }
    }

    private void createNewWorkload(TrainerWorkloadDTO.Request.Create request, YearMonth period, String transactionId) {
        TrainerWorkload workload = mapper.toEntity(request, period);

        log.debug("[{}][Operation] Saving new workload: username={}, period={}, totalHours={}",
                transactionId, workload.getUsername(), workload.getPeriod(), workload.getTotalHours());

        repository.save(workload);

        log.info("[{}][Operation] Created new workload for trainer: {}, totalHours: {}",
                transactionId, request.username(), workload.getTotalHours());
    }

    private void updateExistingWorkload(TrainerWorkload workload, TrainerWorkloadDTO.Request.Create request, String transactionId) {
        double oldHours = workload.getTotalHours();
        double newHours = oldHours + request.trainingDuration();

        log.debug("[{}][Operation] Updating trainer profile: firstName={}, lastName={}, status={}",
                transactionId, request.firstName(), request.lastName(), request.status());

        mapper.updateEntityFromRequest(workload, request);
        workload.setTotalHours(newHours);

        log.debug("[{}][Operation] Saving updated workload: totalHours: {} -> {}",
                transactionId, oldHours, newHours);

        repository.save(workload);

        log.info("[{}][Operation] Updated workload for trainer: {}, added {} hours, new total: {}",
                transactionId, request.username(), request.trainingDuration(), newHours);
    }

    private void deleteTrainingHours(String username, YearMonth period, Double trainingDuration, String transactionId) {
        log.debug("[{}][Operation] Searching for workload to delete: username={}, period={}",
                transactionId, username, period);

        TrainerWorkload workload = repository
                .findByUsernameAndPeriod(username, period)
                .orElseThrow(() -> {
                    log.warn("[{}] Workload not found for trainer {} in period {} when trying to delete hours",
                            transactionId, username, period);
                    return new WorkloadNotFoundException(username, period);
                });

        double oldHours = workload.getTotalHours();
        double newHours = oldHours - trainingDuration;

        if (newHours <= 0) {
            log.debug("[{}][Operation] Deleting workload: totalHours became {} (≤ 0)", transactionId, newHours);
            repository.delete(workload);
            log.info("[{}][Operation] Deleted workload for trainer: {}, period: {}", transactionId, username, period);
        } else {
            workload.setTotalHours(newHours);
            log.debug("[{}][Operation] Saving updated workload: totalHours: {} -> {}", transactionId, oldHours, newHours);
            repository.save(workload);
            log.info("[{}][Operation] Removed {} hours for trainer: {}, new total: {}",
                    transactionId, trainingDuration, username, newHours);
        }
    }
}
