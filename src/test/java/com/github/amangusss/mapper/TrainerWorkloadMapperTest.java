package com.github.amangusss.mapper;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.ActionType;
import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.TrainerStatus;
import com.github.amangusss.entity.TrainerWorkload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrainerWorkloadMapper Tests")
class TrainerWorkloadMapperTest {

    private TrainerWorkloadMapper mapper;

    private static final String USERNAME = "john.doe";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2025, 1, 15);
    private static final YearMonth PERIOD = YearMonth.of(2025, 1);
    private static final Double DURATION = 2.5;

    @BeforeEach
    void setUp() {
        mapper = new TrainerWorkloadMapper();
    }

    private TrainerWorkloadDTO.Request.Create createRequest() {
        return new TrainerWorkloadDTO.Request.Create(
                USERNAME, FIRST_NAME, LAST_NAME,
                TrainerStatus.ACTIVE, TRAINING_DATE, DURATION,
                ActionType.ADD
        );
    }

    private TrainerWorkload createWorkload(YearMonth period, Double totalHours) {
        return TrainerWorkload.builder()
                .id(1L)
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .status(TrainerStatus.ACTIVE)
                .period(period)
                .totalHours(totalHours)
                .build();
    }

    @Nested
    @DisplayName("toEntity Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should map request to entity with correct values")
        void shouldMapRequestToEntity() {
            var request = createRequest();

            var result = mapper.toEntity(request, PERIOD);

            assertThat(result.getUsername()).isEqualTo(USERNAME);
            assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(result.getLastName()).isEqualTo(LAST_NAME);
            assertThat(result.getStatus()).isEqualTo(TrainerStatus.ACTIVE);
            assertThat(result.getPeriod()).isEqualTo(PERIOD);
            assertThat(result.getTotalHours()).isEqualTo(DURATION);
            assertThat(result.getId()).isNull();
        }

        @Test
        @DisplayName("Should set initial totalHours from request duration")
        void shouldSetInitialTotalHoursFromRequestDuration() {
            var request = new TrainerWorkloadDTO.Request.Create(
                    USERNAME, FIRST_NAME, LAST_NAME,
                    TrainerStatus.ACTIVE, TRAINING_DATE, 5.0,
                    ActionType.ADD
            );

            var result = mapper.toEntity(request, PERIOD);

            assertThat(result.getTotalHours()).isEqualTo(5.0);
        }
    }

    @Nested
    @DisplayName("toSummary Tests")
    class ToSummaryTests {

        @Test
        @DisplayName("Should return null for empty workloads list")
        void shouldReturnNullForEmptyList() {
            var result = mapper.toSummary(Collections.emptyList());

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for null workloads list")
        void shouldReturnNullForNullList() {
            var result = mapper.toSummary(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should map single workload to summary")
        void shouldMapSingleWorkloadToSummary() {
            var workloads = List.of(createWorkload(PERIOD, 5.0));

            var result = mapper.toSummary(workloads);

            assertThat(result.username()).isEqualTo(USERNAME);
            assertThat(result.firstName()).isEqualTo(FIRST_NAME);
            assertThat(result.lastName()).isEqualTo(LAST_NAME);
            assertThat(result.status()).isEqualTo(TrainerStatus.ACTIVE);
            assertThat(result.years()).hasSize(1);
            assertThat(result.years().get(0).year()).isEqualTo(2025);
            assertThat(result.years().get(0).months()).hasSize(1);
            assertThat(result.years().get(0).months().get(0).month()).isEqualTo(Month.JANUARY);
            assertThat(result.years().get(0).months().get(0).trainingSummaryDuration()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Should group workloads by year and month")
        void shouldGroupWorkloadsByYearAndMonth() {
            var workloads = List.of(
                    createWorkload(YearMonth.of(2025, 1), 5.0),
                    createWorkload(YearMonth.of(2025, 3), 3.0),
                    createWorkload(YearMonth.of(2024, 12), 2.0)
            );

            var result = mapper.toSummary(workloads);

            assertThat(result.years()).hasSize(2);

            assertThat(result.years().get(0).year()).isEqualTo(2024);
            assertThat(result.years().get(1).year()).isEqualTo(2025);

            assertThat(result.years().get(0).months()).hasSize(1);
            assertThat(result.years().get(0).months().get(0).month()).isEqualTo(Month.DECEMBER);

            assertThat(result.years().get(1).months()).hasSize(2);
        }

        @Test
        @DisplayName("Should sort years in ascending order")
        void shouldSortYearsInAscendingOrder() {
            var workloads = List.of(
                    createWorkload(YearMonth.of(2026, 1), 1.0),
                    createWorkload(YearMonth.of(2024, 1), 2.0),
                    createWorkload(YearMonth.of(2025, 1), 3.0)
            );

            var result = mapper.toSummary(workloads);

            assertThat(result.years()).extracting(TrainerWorkloadDTO.YearSummary::year)
                    .containsExactly(2024, 2025, 2026);
        }
    }

    @Nested
    @DisplayName("toMonthSummary Tests")
    class ToMonthSummaryTests {

        @Test
        @DisplayName("Should map workload to month summary")
        void shouldMapWorkloadToMonthSummary() {
            var workload = createWorkload(YearMonth.of(2025, 3), 7.5);

            var result = mapper.toMonthSummary(workload);

            assertThat(result.month()).isEqualTo(Month.MARCH);
            assertThat(result.trainingSummaryDuration()).isEqualTo(7.5);
        }
    }

    @Nested
    @DisplayName("updateEntityFromRequest Tests")
    class UpdateEntityFromRequestTests {

        @Test
        @DisplayName("Should update entity fields from request")
        void shouldUpdateEntityFieldsFromRequest() {
            var workload = createWorkload(PERIOD, 5.0);
            var request = new TrainerWorkloadDTO.Request.Create(
                    USERNAME, "Jane", "Smith",
                    TrainerStatus.INACTIVE, TRAINING_DATE, DURATION,
                    ActionType.ADD
            );

            mapper.updateEntityFromRequest(workload, request);

            assertThat(workload.getFirstName()).isEqualTo("Jane");
            assertThat(workload.getLastName()).isEqualTo("Smith");
            assertThat(workload.getStatus()).isEqualTo(TrainerStatus.INACTIVE);
        }

        @Test
        @DisplayName("Should not update username and totalHours")
        void shouldNotUpdateUsernameAndTotalHours() {
            var workload = createWorkload(PERIOD, 5.0);
            var request = new TrainerWorkloadDTO.Request.Create(
                    "different.user", FIRST_NAME, LAST_NAME,
                    TrainerStatus.ACTIVE, TRAINING_DATE, 10.0,
                    ActionType.ADD
            );

            mapper.updateEntityFromRequest(workload, request);

            assertThat(workload.getUsername()).isEqualTo(USERNAME);
            assertThat(workload.getTotalHours()).isEqualTo(5.0);
        }
    }
}
