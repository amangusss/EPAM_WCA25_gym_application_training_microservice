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
        YearMonth period = YearMonth.from(request.trainingDate());

        switch (request.actionType()) {
            case ADD -> addTrainingHours(request, period, transactionId);
            case DELETE -> deleteTrainingHours(request.username(), period, request.trainingDuration(), transactionId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerWorkloadDTO.Response.Summary getTrainerSummary(String username, String transactionId) {
        List<TrainerWorkload> workloads = repository.findByUsername(username);

        if (workloads.isEmpty()) {
            log.warn("[{}] Workload not found for trainer {}", transactionId, username);
            throw new TrainerNotFoundException(username);
        }

        return mapper.toSummary(workloads);
    }

    private void addTrainingHours(TrainerWorkloadDTO.Request.Create request, YearMonth period, String transactionId) {
        boolean isNew = !repository.existsByUsernameAndPeriod(request.username(), period);

        TrainerWorkload workload = repository
                .findByUsernameAndPeriod(request.username(), period)
                .orElseGet(() -> mapper.toEntity(request, period));

        double oldHours = workload.getTotalHours();

        if (!isNew) {
            workload.setTotalHours(oldHours + request.trainingDuration());
            mapper.updateEntityFromRequest(workload, request);
        }

        repository.save(workload);

        log.debug("[{}] Added {} hours for trainer {} in period {}. Total: {} -> {}",
                transactionId, request.trainingDuration(), request.username(),
                period, oldHours, workload.getTotalHours());
    }

    private void deleteTrainingHours(String username, YearMonth period, Double trainingDuration, String transactionId) {
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
            repository.delete(workload);
            log.debug("[{}] Deleted workload for trainer {} in period {}. Total: {} -> 0",
                    transactionId, username, period, oldHours);
        } else {
            workload.setTotalHours(newHours);
            repository.save(workload);
            log.debug("[{}] Removed {} hours for trainer {} in period {}. Total: {} -> {}",
                    transactionId, trainingDuration, username, period, oldHours, newHours);
        }
    }
}
