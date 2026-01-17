package com.github.amangusss.mapper;

import com.github.amangusss.dto.generated.MonthSummary;
import com.github.amangusss.dto.generated.TrainerSummaryResponse;
import com.github.amangusss.dto.generated.TrainingEventRequest;
import com.github.amangusss.dto.generated.YearSummary;
import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.ActionType;
import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.TrainerStatus;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.stream.Collectors;

@Component
public class GeneratedDtoMapper {

    public TrainerWorkloadDTO.Request.Create toInternalCreate(TrainingEventRequest request) {
        return new TrainerWorkloadDTO.Request.Create(
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                mapStatus(request.getStatus()),
                request.getTrainingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                request.getTrainingDuration(),
                mapActionType(request.getActionType())
        );
    }

    public TrainerSummaryResponse toGeneratedSummary(TrainerWorkloadDTO.Response.Summary summary) {
        TrainerSummaryResponse response = new TrainerSummaryResponse();
        response.setUsername(summary.username());
        response.setFirstName(summary.firstName());
        response.setLastName(summary.lastName());
        response.setStatus(mapStatusToGenerated(summary.status()));
        response.setYears(summary.years().stream()
                .map(this::mapYearSummary)
                .collect(Collectors.toList()));
        return response;
    }

    private YearSummary mapYearSummary(TrainerWorkloadDTO.YearSummary yearSummary) {
        YearSummary generated = new YearSummary();
        generated.setYear(yearSummary.year());
        generated.setMonths(yearSummary.months().stream()
                .map(this::mapMonthSummary)
                .collect(Collectors.toList()));
        return generated;
    }

    private MonthSummary mapMonthSummary(TrainerWorkloadDTO.MonthSummary monthSummary) {
        MonthSummary generated = new MonthSummary();
        generated.setMonth(mapMonthToGenerated(monthSummary.month()));
        generated.setTrainingSummaryDuration(monthSummary.trainingSummaryDuration());
        return generated;
    }

    private TrainerStatus mapStatus(TrainingEventRequest.StatusEnum statusEnum) {
        return switch (statusEnum) {
            case ACTIVE -> TrainerStatus.ACTIVE;
            case INACTIVE -> TrainerStatus.INACTIVE;
        };
    }

    private TrainerSummaryResponse.StatusEnum mapStatusToGenerated(TrainerStatus status) {
        return switch (status) {
            case ACTIVE -> TrainerSummaryResponse.StatusEnum.ACTIVE;
            case INACTIVE -> TrainerSummaryResponse.StatusEnum.INACTIVE;
        };
    }

    private ActionType mapActionType(TrainingEventRequest.ActionTypeEnum actionTypeEnum) {
        return switch (actionTypeEnum) {
            case ADD -> ActionType.ADD;
            case DELETE -> ActionType.DELETE;
        };
    }

    private MonthSummary.MonthEnum mapMonthToGenerated(Month month) {
        return switch (month) {
            case JANUARY -> MonthSummary.MonthEnum.JANUARY;
            case FEBRUARY -> MonthSummary.MonthEnum.FEBRUARY;
            case MARCH -> MonthSummary.MonthEnum.MARCH;
            case APRIL -> MonthSummary.MonthEnum.APRIL;
            case MAY -> MonthSummary.MonthEnum.MAY;
            case JUNE -> MonthSummary.MonthEnum.JUNE;
            case JULY -> MonthSummary.MonthEnum.JULY;
            case AUGUST -> MonthSummary.MonthEnum.AUGUST;
            case SEPTEMBER -> MonthSummary.MonthEnum.SEPTEMBER;
            case OCTOBER -> MonthSummary.MonthEnum.OCTOBER;
            case NOVEMBER -> MonthSummary.MonthEnum.NOVEMBER;
            case DECEMBER -> MonthSummary.MonthEnum.DECEMBER;
        };
    }
}

