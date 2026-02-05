package com.github.amangusss.mapper;

import com.github.amangusss.dto.generated.MonthSummary;
import com.github.amangusss.dto.generated.TrainerSummaryResponse;
import com.github.amangusss.dto.generated.TrainingEventRequest;
import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.ActionType;
import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.TrainerStatus;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GeneratedDtoMapper Tests")
class GeneratedDtoMapperTest {

    private GeneratedDtoMapper mapper;

    private static final String USERNAME = "john.doe";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    @BeforeEach
    void setUp() {
        mapper = new GeneratedDtoMapper();
    }

    @Nested
    @DisplayName("toInternalCreate Tests")
    class ToInternalCreateTests {

        @Test
        @DisplayName("Should map ADD training event request to internal DTO")
        void shouldMapAddTrainingEventRequestToInternalDto() {
            LocalDate trainingDate = LocalDate.of(2025, 1, 15);
            Instant instant = trainingDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Date date = Date.from(instant);

            TrainingEventRequest request = new TrainingEventRequest();
            request.setUsername(USERNAME);
            request.setFirstName(FIRST_NAME);
            request.setLastName(LAST_NAME);
            request.setStatus(TrainingEventRequest.StatusEnum.ACTIVE);
            request.setTrainingDate(date);
            request.setTrainingDuration(2.5);
            request.setActionType(TrainingEventRequest.ActionTypeEnum.ADD);

            var result = mapper.toInternalCreate(request);

            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo(USERNAME);
            assertThat(result.firstName()).isEqualTo(FIRST_NAME);
            assertThat(result.lastName()).isEqualTo(LAST_NAME);
            assertThat(result.status()).isEqualTo(TrainerStatus.ACTIVE);
            assertThat(result.trainingDate()).isEqualTo(trainingDate);
            assertThat(result.trainingDuration()).isEqualTo(2.5);
            assertThat(result.actionType()).isEqualTo(ActionType.ADD);
        }

        @Test
        @DisplayName("Should map DELETE training event request to internal DTO")
        void shouldMapDeleteTrainingEventRequestToInternalDto() {
            LocalDate trainingDate = LocalDate.of(2025, 6, 20);
            Instant instant = trainingDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Date date = Date.from(instant);

            TrainingEventRequest request = new TrainingEventRequest();
            request.setUsername(USERNAME);
            request.setFirstName(FIRST_NAME);
            request.setLastName(LAST_NAME);
            request.setStatus(TrainingEventRequest.StatusEnum.INACTIVE);
            request.setTrainingDate(date);
            request.setTrainingDuration(3.5);
            request.setActionType(TrainingEventRequest.ActionTypeEnum.DELETE);

            var result = mapper.toInternalCreate(request);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(TrainerStatus.INACTIVE);
            assertThat(result.actionType()).isEqualTo(ActionType.DELETE);
            assertThat(result.trainingDuration()).isEqualTo(3.5);
        }

        @Test
        @DisplayName("Should correctly convert date to LocalDate")
        void shouldCorrectlyConvertDateToLocalDate() {
            LocalDate expectedDate = LocalDate.of(2026, 12, 31);
            Instant instant = expectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Date date = Date.from(instant);

            TrainingEventRequest request = new TrainingEventRequest();
            request.setUsername(USERNAME);
            request.setFirstName(FIRST_NAME);
            request.setLastName(LAST_NAME);
            request.setStatus(TrainingEventRequest.StatusEnum.ACTIVE);
            request.setTrainingDate(date);
            request.setTrainingDuration(1.0);
            request.setActionType(TrainingEventRequest.ActionTypeEnum.ADD);

            var result = mapper.toInternalCreate(request);

            assertThat(result.trainingDate()).isEqualTo(expectedDate);
        }
    }

    @Nested
    @DisplayName("toGeneratedSummary Tests")
    class ToGeneratedSummaryTests {

        @Test
        @DisplayName("Should map internal summary to generated response")
        void shouldMapInternalSummaryToGeneratedResponse() {
            TrainerWorkloadDTO.Response.Summary summary = getSummary();

            TrainerSummaryResponse result = mapper.toGeneratedSummary(summary);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(USERNAME);
            assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(result.getLastName()).isEqualTo(LAST_NAME);
            assertThat(result.getStatus()).isEqualTo(TrainerSummaryResponse.StatusEnum.ACTIVE);
            assertThat(result.getYears()).hasSize(1);
            assertThat(result.getYears().get(0).getYear()).isEqualTo(2025);
            assertThat(result.getYears().get(0).getMonths()).hasSize(1);
            assertThat(result.getYears().get(0).getMonths().get(0).getMonth())
                    .isEqualTo(MonthSummary.MonthEnum.JANUARY);
            assertThat(result.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration())
                    .isEqualTo(10.5);
        }

        private TrainerWorkloadDTO.Response.@NonNull Summary getSummary() {
            TrainerWorkloadDTO.MonthSummary monthSummary = new TrainerWorkloadDTO.MonthSummary(
                    Month.JANUARY,
                    10.5
            );

            TrainerWorkloadDTO.YearSummary yearSummary = new TrainerWorkloadDTO.YearSummary(
                    2025,
                    List.of(monthSummary)
            );

            return new TrainerWorkloadDTO.Response.Summary(
                    USERNAME,
                    FIRST_NAME,
                    LAST_NAME,
                    TrainerStatus.ACTIVE,
                    List.of(yearSummary)
            );
        }

        @Test
        @DisplayName("Should map INACTIVE status correctly")
        void shouldMapInactiveStatusCorrectly() {
            TrainerWorkloadDTO.Response.Summary summary = new TrainerWorkloadDTO.Response.Summary(
                    USERNAME,
                    FIRST_NAME,
                    LAST_NAME,
                    TrainerStatus.INACTIVE,
                    List.of()
            );

            TrainerSummaryResponse result = mapper.toGeneratedSummary(summary);

            assertThat(result.getStatus()).isEqualTo(TrainerSummaryResponse.StatusEnum.INACTIVE);
        }

        @Test
        @DisplayName("Should map multiple years and months correctly")
        void shouldMapMultipleYearsAndMonthsCorrectly() {
            TrainerWorkloadDTO.MonthSummary jan = new TrainerWorkloadDTO.MonthSummary(Month.JANUARY, 5.0);
            TrainerWorkloadDTO.MonthSummary feb = new TrainerWorkloadDTO.MonthSummary(Month.FEBRUARY, 8.0);
            TrainerWorkloadDTO.Response.Summary summary = getSummary(jan, feb);

            TrainerSummaryResponse result = mapper.toGeneratedSummary(summary);

            assertThat(result.getYears()).hasSize(2);
            assertThat(result.getYears().get(0).getMonths()).hasSize(2);
            assertThat(result.getYears().get(1).getMonths()).hasSize(1);
        }

        private TrainerWorkloadDTO.Response.@NonNull Summary getSummary(TrainerWorkloadDTO.MonthSummary jan, TrainerWorkloadDTO.MonthSummary feb) {
            TrainerWorkloadDTO.MonthSummary mar = new TrainerWorkloadDTO.MonthSummary(Month.MARCH, 12.0);

            TrainerWorkloadDTO.YearSummary year2025 = new TrainerWorkloadDTO.YearSummary(2025, List.of(jan, feb));
            TrainerWorkloadDTO.YearSummary year2026 = new TrainerWorkloadDTO.YearSummary(2026, List.of(mar));

            return new TrainerWorkloadDTO.Response.Summary(
                    USERNAME,
                    FIRST_NAME,
                    LAST_NAME,
                    TrainerStatus.ACTIVE,
                    List.of(year2025, year2026)
            );
        }

        @Test
        @DisplayName("Should map all months correctly")
        void shouldMapAllMonthsCorrectly() {
            TrainerWorkloadDTO.YearSummary yearSummary = getYearSummary();

            TrainerWorkloadDTO.Response.Summary summary = new TrainerWorkloadDTO.Response.Summary(
                    USERNAME,
                    FIRST_NAME,
                    LAST_NAME,
                    TrainerStatus.ACTIVE,
                    List.of(yearSummary)
            );

            TrainerSummaryResponse result = mapper.toGeneratedSummary(summary);

            assertThat(result.getYears().get(0).getMonths()).hasSize(12);
            assertThat(result.getYears().get(0).getMonths().get(0).getMonth())
                    .isEqualTo(MonthSummary.MonthEnum.JANUARY);
            assertThat(result.getYears().get(0).getMonths().get(11).getMonth())
                    .isEqualTo(MonthSummary.MonthEnum.DECEMBER);
        }

        private static TrainerWorkloadDTO.@NonNull YearSummary getYearSummary() {
            TrainerWorkloadDTO.MonthSummary[] months = {
                    new TrainerWorkloadDTO.MonthSummary(Month.JANUARY, 1.0),
                    new TrainerWorkloadDTO.MonthSummary(Month.FEBRUARY, 2.0),
                    new TrainerWorkloadDTO.MonthSummary(Month.MARCH, 3.0),
                    new TrainerWorkloadDTO.MonthSummary(Month.APRIL, 4.0),
                    new TrainerWorkloadDTO.MonthSummary(Month.MAY, 5.0),
                    new TrainerWorkloadDTO.MonthSummary(Month.JUNE, 6.0),
                    new TrainerWorkloadDTO.MonthSummary(Month.JULY, 7.0),
                    new TrainerWorkloadDTO.MonthSummary(Month.AUGUST, 8.0),
                    new TrainerWorkloadDTO.MonthSummary(Month.SEPTEMBER, 9.0),
                    new TrainerWorkloadDTO.MonthSummary(Month.OCTOBER, 10.0),
                    new TrainerWorkloadDTO.MonthSummary(Month.NOVEMBER, 11.0),
                    new TrainerWorkloadDTO.MonthSummary(Month.DECEMBER, 12.0)
            };

            return new TrainerWorkloadDTO.YearSummary(2025, List.of(months));
        }

        @Test
        @DisplayName("Should handle empty years list")
        void shouldHandleEmptyYearsList() {
            TrainerWorkloadDTO.Response.Summary summary = new TrainerWorkloadDTO.Response.Summary(
                    USERNAME,
                    FIRST_NAME,
                    LAST_NAME,
                    TrainerStatus.ACTIVE,
                    List.of()
            );

            TrainerSummaryResponse result = mapper.toGeneratedSummary(summary);

            assertThat(result.getYears()).isEmpty();
        }
    }
}
