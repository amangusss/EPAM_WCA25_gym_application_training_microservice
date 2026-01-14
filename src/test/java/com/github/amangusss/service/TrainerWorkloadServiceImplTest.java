package com.github.amangusss.service;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.TrainerWorkload;
import com.github.amangusss.entity.MonthSummary;
import com.github.amangusss.entity.YearSummary;
import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.TrainerStatus;
import com.github.amangusss.entity.ActionType;
import com.github.amangusss.exception.TrainerNotFoundException;
import com.github.amangusss.mapper.TrainerWorkloadMapper;
import com.github.amangusss.repository.TrainerWorkloadRepository;
import com.github.amangusss.service.impl.TrainerWorkloadServiceImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerWorkloadService Tests")
class TrainerWorkloadServiceImplTest {

    @Mock
    private TrainerWorkloadRepository repository;

    @Mock
    private TrainerWorkloadMapper mapper;

    @InjectMocks
    private TrainerWorkloadServiceImpl service;

    @Captor
    private ArgumentCaptor<TrainerWorkload> workloadCaptor;

    private static final String TRANSACTION_ID = "test-transaction-id";
    private static final String USERNAME = "john.doe";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2025, 1, 15);
    private static final Double DURATION = 2.5;

    private TrainerWorkloadDTO.Request.Create createAddRequest() {
        return new TrainerWorkloadDTO.Request.Create(
                USERNAME, FIRST_NAME, LAST_NAME,
                TrainerStatus.ACTIVE, TRAINING_DATE, DURATION,
                ActionType.ADD
        );
    }

    private TrainerWorkloadDTO.Request.Create createDeleteRequest() {
        return new TrainerWorkloadDTO.Request.Create(
                USERNAME, FIRST_NAME, LAST_NAME,
                TrainerStatus.ACTIVE, TRAINING_DATE, DURATION,
                ActionType.DELETE
        );
    }

    private TrainerWorkload createWorkloadWithHours(Double totalHours) {
        MonthSummary monthSummary = MonthSummary.builder()
                .month(Month.JANUARY)
                .totalHours(totalHours)
                .build();

        YearSummary yearSummary = YearSummary.builder()
                .year(2025)
                .months(new ArrayList<>(List.of(monthSummary)))
                .build();

        return TrainerWorkload.builder()
                .id("test-id")
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .status(TrainerStatus.ACTIVE)
                .years(new ArrayList<>(List.of(yearSummary)))
                .build();
    }

    @Nested
    @DisplayName("ADD Training Hours")
    class AddTrainingHoursTests {

        @Test
        @DisplayName("Should create new workload when no existing record")
        void shouldCreateNewWorkloadWhenNoExistingRecord() {
            var request = createAddRequest();

            when(repository.findByUsername(USERNAME)).thenReturn(Optional.empty());
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();
            assertThat(saved.getUsername()).isEqualTo(USERNAME);
            assertThat(saved.getYears()).hasSize(1);
            assertThat(saved.getYears().get(0).getMonths()).hasSize(1);
            assertThat(saved.getYears().get(0).getMonths().get(0).getTotalHours()).isEqualTo(DURATION);
        }

        @Test
        @DisplayName("Should add hours to existing workload")
        void shouldAddHoursToExistingWorkload() {
            var request = createAddRequest();
            var existingWorkload = createWorkloadWithHours(5.0);

            when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(existingWorkload));
            doNothing().when(mapper).updateWorkloadInfo(any(), any());
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();
            assertThat(saved.getYears().get(0).getMonths().get(0).getTotalHours()).isEqualTo(7.5); // 5.0 + 2.5
        }

        @Test
        @DisplayName("Should accumulate multiple trainings in same month")
        void shouldAccumulateMultipleTrainingsInSameMonth() {
            var request = createAddRequest();
            var existingWorkload = createWorkloadWithHours(10.0);

            when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(existingWorkload));
            doNothing().when(mapper).updateWorkloadInfo(any(), any());
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();
            assertThat(saved.getYears().get(0).getMonths().get(0).getTotalHours()).isEqualTo(12.5); // 10.0 + 2.5
        }
    }

    @Nested
    @DisplayName("DELETE Training Hours")
    class DeleteTrainingHoursTests {

        @Test
        @DisplayName("Should subtract hours from existing workload")
        void shouldSubtractHoursFromExistingWorkload() {
            var request = createDeleteRequest();
            var existingWorkload = createWorkloadWithHours(5.0);

            when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(existingWorkload));
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();
            assertThat(saved.getYears().get(0).getMonths().get(0).getTotalHours()).isEqualTo(2.5); // 5.0 - 2.5
        }

        @Test
        @DisplayName("Should delete month when hours become zero")
        void shouldDeleteMonthWhenHoursBecomeZero() {
            var request = createDeleteRequest();
            var existingWorkload = createWorkloadWithHours(2.5);

            when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(existingWorkload));
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();
            assertThat(saved.getYears()).satisfiesAnyOf(
                    years -> assertThat(years).isEmpty(),
                    years -> assertThat(years.get(0).getMonths()).isEmpty()
            );
        }

        @Test
        @DisplayName("Should delete month when hours become negative")
        void shouldDeleteMonthWhenHoursBecomeNegative() {
            var request = createDeleteRequest();
            var existingWorkload = createWorkloadWithHours(1.0);

            when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(existingWorkload));
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();
            // Проверяем что список годов пустой или первый год не имеет месяцев
            assertThat(saved.getYears()).satisfiesAnyOf(
                    years -> assertThat(years).isEmpty(),
                    years -> assertThat(years.get(0).getMonths()).isEmpty()
            );
        }

        @Test
        @DisplayName("Should throw exception when workload not found for delete")
        void shouldThrowExceptionWhenWorkloadNotFoundForDelete() {
            var request = createDeleteRequest();

            when(repository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.obtainWorkload(request, TRANSACTION_ID))
                    .isInstanceOf(TrainerNotFoundException.class);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("GET Trainer Summary")
    class GetTrainerSummaryTests {

        @Test
        @DisplayName("Should return trainer summary")
        void shouldReturnTrainerSummary() {
            var workload = createWorkloadWithHours(5.0);

            var expectedSummary = new TrainerWorkloadDTO.Response.Summary(
                    USERNAME, FIRST_NAME, LAST_NAME,
                    TrainerStatus.ACTIVE,
                    List.of(new TrainerWorkloadDTO.YearSummary(2025, List.of(
                            new TrainerWorkloadDTO.MonthSummary(Month.JANUARY, 5.0)
                    )))
            );

            when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(workload));
            when(mapper.toSummary(workload)).thenReturn(expectedSummary);

            var result = service.getTrainerSummary(USERNAME, TRANSACTION_ID);

            assertThat(result).isEqualTo(expectedSummary);
            verify(repository).findByUsername(USERNAME);
            verify(mapper).toSummary(workload);
        }

        @Test
        @DisplayName("Should throw exception when trainer not found")
        void shouldThrowExceptionWhenTrainerNotFound() {
            when(repository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getTrainerSummary(USERNAME, TRANSACTION_ID))
                    .isInstanceOf(TrainerNotFoundException.class);

            verify(mapper, never()).toSummary(any());
        }
    }
}
