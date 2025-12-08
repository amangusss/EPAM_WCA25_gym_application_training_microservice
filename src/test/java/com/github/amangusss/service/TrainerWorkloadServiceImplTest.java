package com.github.amangusss.service;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.ActionType;
import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.TrainerStatus;
import com.github.amangusss.entity.TrainerWorkload;
import com.github.amangusss.exception.TrainerNotFoundException;
import com.github.amangusss.exception.WorkloadNotFoundException;
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
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    private static final YearMonth PERIOD = YearMonth.of(2025, 1);
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

    private TrainerWorkload createWorkload(Double totalHours) {
        return TrainerWorkload.builder()
                .id(1L)
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .status(TrainerStatus.ACTIVE)
                .period(PERIOD)
                .totalHours(totalHours)
                .build();
    }

    @Nested
    @DisplayName("ADD Training Hours")
    class AddTrainingHoursTests {

        @Test
        @DisplayName("Should create new workload when no existing record")
        void shouldCreateNewWorkloadWhenNoExistingRecord() {
            var request = createAddRequest();
            var newWorkload = createWorkload(DURATION);

            when(repository.existsByUsernameAndPeriod(USERNAME, PERIOD)).thenReturn(false);
            when(repository.findByUsernameAndPeriod(USERNAME, PERIOD)).thenReturn(Optional.empty());
            when(mapper.toEntity(request, PERIOD)).thenReturn(newWorkload);
            when(repository.save(any(TrainerWorkload.class))).thenReturn(newWorkload);

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            assertThat(workloadCaptor.getValue().getTotalHours()).isEqualTo(DURATION);
            verify(mapper).toEntity(request, PERIOD);
            verify(mapper, never()).updateEntityFromRequest(any(), any());
        }

        @Test
        @DisplayName("Should add hours to existing workload")
        void shouldAddHoursToExistingWorkload() {
            var request = createAddRequest();
            var existingWorkload = createWorkload(5.0);

            when(repository.existsByUsernameAndPeriod(USERNAME, PERIOD)).thenReturn(true);
            when(repository.findByUsernameAndPeriod(USERNAME, PERIOD)).thenReturn(Optional.of(existingWorkload));
            when(repository.save(any(TrainerWorkload.class))).thenReturn(existingWorkload);

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            assertThat(workloadCaptor.getValue().getTotalHours()).isEqualTo(7.5); // 5.0 + 2.5
            verify(mapper).updateEntityFromRequest(existingWorkload, request);
            verify(mapper, never()).toEntity(any(), any());
        }

        @Test
        @DisplayName("Should accumulate multiple trainings in same month")
        void shouldAccumulateMultipleTrainingsInSameMonth() {
            var request = createAddRequest();
            var existingWorkload = createWorkload(10.0);

            when(repository.existsByUsernameAndPeriod(USERNAME, PERIOD)).thenReturn(true);
            when(repository.findByUsernameAndPeriod(USERNAME, PERIOD)).thenReturn(Optional.of(existingWorkload));
            when(repository.save(any(TrainerWorkload.class))).thenReturn(existingWorkload);

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            assertThat(workloadCaptor.getValue().getTotalHours()).isEqualTo(12.5); // 10.0 + 2.5
        }
    }

    @Nested
    @DisplayName("DELETE Training Hours")
    class DeleteTrainingHoursTests {

        @Test
        @DisplayName("Should subtract hours from existing workload")
        void shouldSubtractHoursFromExistingWorkload() {
            var request = createDeleteRequest();
            var existingWorkload = createWorkload(5.0);

            when(repository.findByUsernameAndPeriod(USERNAME, PERIOD)).thenReturn(Optional.of(existingWorkload));
            when(repository.save(any(TrainerWorkload.class))).thenReturn(existingWorkload);

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            assertThat(workloadCaptor.getValue().getTotalHours()).isEqualTo(2.5); // 5.0 - 2.5
            verify(repository, never()).delete(any());
        }

        @Test
        @DisplayName("Should delete workload when hours become zero")
        void shouldDeleteWorkloadWhenHoursBecomeZero() {
            var request = createDeleteRequest();
            var existingWorkload = createWorkload(2.5);

            when(repository.findByUsernameAndPeriod(USERNAME, PERIOD)).thenReturn(Optional.of(existingWorkload));

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).delete(existingWorkload);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Should delete workload when hours become negative")
        void shouldDeleteWorkloadWhenHoursBecomeNegative() {
            var request = createDeleteRequest();
            var existingWorkload = createWorkload(1.0);

            when(repository.findByUsernameAndPeriod(USERNAME, PERIOD)).thenReturn(Optional.of(existingWorkload));

            service.obtainWorkload(request, TRANSACTION_ID);

            verify(repository).delete(existingWorkload);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when workload not found for delete")
        void shouldThrowExceptionWhenWorkloadNotFoundForDelete() {
            var request = createDeleteRequest();

            when(repository.findByUsernameAndPeriod(USERNAME, PERIOD)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.obtainWorkload(request, TRANSACTION_ID))
                    .isInstanceOf(WorkloadNotFoundException.class);

            verify(repository, never()).delete(any());
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("GET Trainer Summary")
    class GetTrainerSummaryTests {

        @Test
        @DisplayName("Should return trainer summary")
        void shouldReturnTrainerSummary() {
            var workloads = List.of(
                    createWorkload(5.0),
                    TrainerWorkload.builder()
                            .username(USERNAME)
                            .firstName(FIRST_NAME)
                            .lastName(LAST_NAME)
                            .status(TrainerStatus.ACTIVE)
                            .period(YearMonth.of(2025, 2))
                            .totalHours(3.0)
                            .build()
            );

            var expectedSummary = new TrainerWorkloadDTO.Response.Summary(
                    USERNAME, FIRST_NAME, LAST_NAME,
                    TrainerStatus.ACTIVE,
                    List.of(new TrainerWorkloadDTO.YearSummary(2025, List.of(
                            new TrainerWorkloadDTO.MonthSummary(Month.of(1), 5.0),
                            new TrainerWorkloadDTO.MonthSummary(Month.of(2), 3.0)
                    )))
            );

            when(repository.findByUsername(USERNAME)).thenReturn(workloads);
            when(mapper.toSummary(workloads)).thenReturn(expectedSummary);

            var result = service.getTrainerSummary(USERNAME, TRANSACTION_ID);

            assertThat(result).isEqualTo(expectedSummary);
            verify(repository).findByUsername(USERNAME);
            verify(mapper).toSummary(workloads);
        }

        @Test
        @DisplayName("Should throw exception when trainer not found")
        void shouldThrowExceptionWhenTrainerNotFound() {
            when(repository.findByUsername(USERNAME)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> service.getTrainerSummary(USERNAME, TRANSACTION_ID))
                    .isInstanceOf(TrainerNotFoundException.class);

            verify(mapper, never()).toSummary(any());
        }
    }
}
