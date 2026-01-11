package com.github.amangusss.dto.trainerWorkload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.amangusss.converter.jackson.TrainerStatusDeserializer;
import com.github.amangusss.entity.ActionType;
import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.TrainerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public class TrainerWorkloadDTO {

    private TrainerWorkloadDTO() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static class Request {
        public record Create(
                @NotBlank String username,
                @NotBlank String firstName,
                @NotBlank String lastName,
                @JsonProperty("isActive")
                @JsonDeserialize(using = TrainerStatusDeserializer.class)
                @NotNull TrainerStatus status,
                @NotNull LocalDate trainingDate,
                @NotNull @Positive Double trainingDuration,
                @NotNull ActionType actionType
                ) {}
    }

    public static class Response {
        public record Summary(
                String username,
                String firstName,
                String lastName,
                TrainerStatus status,
                List<YearSummary> years
        ){}
    }

    public record YearSummary(
            int year,
            List<MonthSummary> months
    ) {}

    public record MonthSummary(
            Month month,
            double trainingSummaryDuration
    ) {}
}
