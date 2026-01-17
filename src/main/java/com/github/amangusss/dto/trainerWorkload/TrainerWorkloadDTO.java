package com.github.amangusss.dto.trainerWorkload;

import com.github.amangusss.entity.ActionType;
import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.TrainerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public class TrainerWorkloadDTO {

    private TrainerWorkloadDTO() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static class Request {
        public record Create(
                @NotBlank(message = "Username is required")
                String username,

                @NotBlank(message = "First name is required")
                String firstName,

                @NotBlank(message = "Last name is required")
                String lastName,

                @NotNull(message = "Trainer status is required")
                TrainerStatus status,

                @NotNull(message = "Training date is required")
                @PastOrPresent(message = "Training date cannot be in the future")
                LocalDate trainingDate,

                @NotNull(message = "Training duration is required")
                @Positive(message = "Training duration must be positive")
                Double trainingDuration,

                @NotNull(message = "Action type is required")
                ActionType actionType
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
