package com.github.amangusss.service.impl;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.MonthSummary;
import com.github.amangusss.entity.TrainerWorkload;
import com.github.amangusss.entity.YearSummary;
import com.github.amangusss.entity.Month;
import com.github.amangusss.exception.TrainerNotFoundException;
import com.github.amangusss.mapper.TrainerWorkloadMapper;
import com.github.amangusss.repository.TrainerWorkloadRepository;
import com.github.amangusss.service.TrainerWorkloadService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

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

        LocalDate trainingDate = request.trainingDate();
        int year = trainingDate.getYear();
        Month month = Month.of(trainingDate.getMonthValue());

        log.debug("[{}][Operation] Calculated year {} and month {} for training date {}",
                transactionId, year, month, trainingDate);

        switch (request.actionType()) {
            case ADD -> addTrainingHours(request, year, month, transactionId);
            case DELETE -> deleteTrainingHours(request.username(), year, month, request.trainingDuration(), transactionId);
        }

        log.info("[{}][Transaction] Completed processing training event for trainer: {}",
                transactionId, request.username());
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerWorkloadDTO.Response.Summary getTrainerSummary(String username, String transactionId) {
        log.info("[{}][Transaction] Retrieving workload summary for trainer {}", transactionId, username);

        TrainerWorkload workload = repository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("[{}] Workload not found for trainer {}", transactionId, username);
                    return new TrainerNotFoundException(username);
                });

        log.debug("[{}][Operation] Found workload for trainer {}", transactionId, username);

        TrainerWorkloadDTO.Response.Summary summary = mapper.toSummary(workload);
        log.info("[{}][Transaction] Successfully retrieved summary for trainer: {}", transactionId, username);

        return summary;
    }

    private void addTrainingHours(TrainerWorkloadDTO.Request.Create request, int year, Month month, String transactionId) {
        log.debug("[{}][Operation] Searching for existing workload: username={}", transactionId, request.username());

        TrainerWorkload workload = repository.findByUsername(request.username())
                .orElseGet(() -> createNewWorkload(request, transactionId));

        mapper.updateWorkloadInfo(workload, request);

        YearSummary yearSummary = findOrCreateYear(workload, year);
        MonthSummary monthSummary = findOrCreateMonth(yearSummary, month);

        double oldHours = monthSummary.getTotalHours();
        double newHours = oldHours + request.trainingDuration();
        monthSummary.setTotalHours(newHours);

        log.debug("[{}][Operation] Saving updated workload: totalHours: {} -> {}", transactionId, oldHours, newHours);
        repository.save(workload);

        log.info("[{}][Operation] Added {} hours for trainer: {}, new total: {}",
                transactionId, request.trainingDuration(), request.username(), newHours);
    }

    private TrainerWorkload createNewWorkload(TrainerWorkloadDTO.Request.Create request, String transactionId) {
        log.debug("[{}][Operation] Creating new workload for trainer: {}", transactionId, request.username());

        return TrainerWorkload.builder()
                .username(request.username())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .status(request.status())
                .years(new ArrayList<>())
                .build();
    }

    private YearSummary findOrCreateYear(TrainerWorkload workload, int year) {
        return workload.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = YearSummary.builder()
                            .year(year)
                            .months(new ArrayList<>())
                            .build();
                    workload.getYears().add(newYear);
                    return newYear;
                });
    }

    private MonthSummary findOrCreateMonth(YearSummary yearSummary, Month month) {
        return yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary newMonth = MonthSummary.builder()
                            .month(month)
                            .totalHours(0.0)
                            .build();
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });
    }

    private void deleteTrainingHours(String username, int year, Month month, Double trainingDuration, String transactionId) {
        log.debug("[{}][Operation] Searching for workload to delete: username={}", transactionId, username);

        TrainerWorkload workload = repository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("[{}] Workload not found for trainer {} when trying to delete hours", transactionId, username);
                    return new TrainerNotFoundException(username);
                });

        YearSummary yearSummary = workload.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElseThrow(() -> new TrainerNotFoundException(username));

        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseThrow(() -> new TrainerNotFoundException(username));

        double oldHours = monthSummary.getTotalHours();
        double newHours = oldHours - trainingDuration;

        if (newHours <= 0) {
            log.debug("[{}][Operation] Removing month: totalHours became {} (≤ 0)", transactionId, newHours);
            yearSummary.getMonths().remove(monthSummary);

            if (yearSummary.getMonths().isEmpty()) {
                log.debug("[{}][Operation] Removing year: no months left", transactionId);
                workload.getYears().remove(yearSummary);
            }
        } else {
            monthSummary.setTotalHours(newHours);
        }

        log.debug("[{}][Operation] Saving updated workload", transactionId);
        repository.save(workload);

        log.info("[{}][Operation] Removed {} hours for trainer: {}", transactionId, trainingDuration, username);
    }
}
