package com.github.amangusss.mapper;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.TrainerWorkload;
import com.github.amangusss.entity.MonthSummary;
import com.github.amangusss.entity.YearSummary;
import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.TrainerStatus;
import com.github.amangusss.entity.ActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrainerWorkloadMapper Tests")
class TrainerWorkloadMapperTest {

    private TrainerWorkloadMapper mapper;

    private static final String USERNAME = "john.doe";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    @BeforeEach
    void setUp() {
        mapper = new TrainerWorkloadMapper();
    }

    @Nested
    @DisplayName("toSummary Tests")
    class ToSummaryTests {

        @Test
        @DisplayName("Should return null for null workload")
        void shouldReturnNullForNullWorkload() {
            var result = mapper.toSummary(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should map workload to summary with single year and month")
        void shouldMapWorkloadToSummaryWithSingleYearAndMonth() {
            MonthSummary monthSummary = MonthSummary.builder()
                    .month(Month.JANUARY)
                    .totalHours(10.0)
                    .build();

            YearSummary yearSummary = YearSummary.builder()
                    .year(2025)
                    .months(new ArrayList<>(List.of(monthSummary)))
                    .build();

            TrainerWorkload workload = TrainerWorkload.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .status(TrainerStatus.ACTIVE)
                    .years(new ArrayList<>(List.of(yearSummary)))
                    .build();

            var result = mapper.toSummary(workload);

            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo(USERNAME);
            assertThat(result.firstName()).isEqualTo(FIRST_NAME);
            assertThat(result.lastName()).isEqualTo(LAST_NAME);
            assertThat(result.status()).isEqualTo(TrainerStatus.ACTIVE);
            assertThat(result.years()).hasSize(1);
            assertThat(result.years().get(0).year()).isEqualTo(2025);
            assertThat(result.years().get(0).months()).hasSize(1);
            assertThat(result.years().get(0).months().get(0).month()).isEqualTo(Month.JANUARY);
            assertThat(result.years().get(0).months().get(0).trainingSummaryDuration()).isEqualTo(10.0);
        }

        @Test
        @DisplayName("Should map workload with multiple years and months")
        void shouldMapWorkloadWithMultipleYearsAndMonths() {
            MonthSummary jan2025 = MonthSummary.builder()
                    .month(Month.JANUARY)
                    .totalHours(5.0)
                    .build();

            MonthSummary feb2025 = MonthSummary.builder()
                    .month(Month.FEBRUARY)
                    .totalHours(8.0)
                    .build();

            YearSummary year2025 = YearSummary.builder()
                    .year(2025)
                    .months(new ArrayList<>(List.of(jan2025, feb2025)))
                    .build();

            MonthSummary mar2026 = MonthSummary.builder()
                    .month(Month.MARCH)
                    .totalHours(12.0)
                    .build();

            YearSummary year2026 = YearSummary.builder()
                    .year(2026)
                    .months(new ArrayList<>(List.of(mar2026)))
                    .build();

            TrainerWorkload workload = TrainerWorkload.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .status(TrainerStatus.ACTIVE)
                    .years(new ArrayList<>(List.of(year2025, year2026)))
                    .build();

            var result = mapper.toSummary(workload);

            assertThat(result).isNotNull();
            assertThat(result.years()).hasSize(2);
            assertThat(result.years().get(0).year()).isEqualTo(2025);
            assertThat(result.years().get(0).months()).hasSize(2);
            assertThat(result.years().get(1).year()).isEqualTo(2026);
            assertThat(result.years().get(1).months()).hasSize(1);
        }

        @Test
        @DisplayName("Should sort years in ascending order")
        void shouldSortYearsInAscendingOrder() {
            YearSummary year2026 = YearSummary.builder()
                    .year(2026)
                    .months(new ArrayList<>())
                    .build();

            YearSummary year2024 = YearSummary.builder()
                    .year(2024)
                    .months(new ArrayList<>())
                    .build();

            YearSummary year2025 = YearSummary.builder()
                    .year(2025)
                    .months(new ArrayList<>())
                    .build();

            TrainerWorkload workload = TrainerWorkload.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .status(TrainerStatus.ACTIVE)
                    .years(new ArrayList<>(List.of(year2026, year2024, year2025)))
                    .build();

            var result = mapper.toSummary(workload);

            assertThat(result.years()).hasSize(3);
            assertThat(result.years().get(0).year()).isEqualTo(2024);
            assertThat(result.years().get(1).year()).isEqualTo(2025);
            assertThat(result.years().get(2).year()).isEqualTo(2026);
        }

        @Test
        @DisplayName("Should map workload with empty years list")
        void shouldMapWorkloadWithEmptyYearsList() {
            TrainerWorkload workload = TrainerWorkload.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .status(TrainerStatus.INACTIVE)
                    .years(new ArrayList<>())
                    .build();

            var result = mapper.toSummary(workload);

            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo(USERNAME);
            assertThat(result.status()).isEqualTo(TrainerStatus.INACTIVE);
            assertThat(result.years()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateWorkloadInfo Tests")
    class UpdateWorkloadInfoTests {

        @Test
        @DisplayName("Should update workload info from request")
        void shouldUpdateWorkloadInfoFromRequest() {
            TrainerWorkload workload = TrainerWorkload.builder()
                    .username(USERNAME)
                    .firstName("OldFirstName")
                    .lastName("OldLastName")
                    .status(TrainerStatus.INACTIVE)
                    .years(new ArrayList<>())
                    .build();

            TrainerWorkloadDTO.Request.Create request = new TrainerWorkloadDTO.Request.Create(
                    USERNAME,
                    "NewFirstName",
                    "NewLastName",
                    TrainerStatus.ACTIVE,
                    null,
                    5.0,
                    ActionType.ADD
            );

            mapper.updateWorkloadInfo(workload, request);

            assertThat(workload.getFirstName()).isEqualTo("NewFirstName");
            assertThat(workload.getLastName()).isEqualTo("NewLastName");
            assertThat(workload.getStatus()).isEqualTo(TrainerStatus.ACTIVE);
            assertThat(workload.getUsername()).isEqualTo(USERNAME); // username should not change
        }

        @Test
        @DisplayName("Should handle status change from ACTIVE to INACTIVE")
        void shouldHandleStatusChangeFromActiveToInactive() {
            TrainerWorkload workload = TrainerWorkload.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .status(TrainerStatus.ACTIVE)
                    .years(new ArrayList<>())
                    .build();

            TrainerWorkloadDTO.Request.Create request = new TrainerWorkloadDTO.Request.Create(
                    USERNAME,
                    FIRST_NAME,
                    LAST_NAME,
                    TrainerStatus.INACTIVE,
                    null,
                    5.0,
                    ActionType.ADD
            );

            mapper.updateWorkloadInfo(workload, request);

            assertThat(workload.getStatus()).isEqualTo(TrainerStatus.INACTIVE);
        }
    }
}
