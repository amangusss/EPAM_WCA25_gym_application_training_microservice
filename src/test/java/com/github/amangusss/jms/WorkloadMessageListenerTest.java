package com.github.amangusss.jms;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.ActionType;
import com.github.amangusss.entity.TrainerStatus;
import com.github.amangusss.jms.listener.WorkloadMessageListener;
import com.github.amangusss.service.TrainerWorkloadService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadMessageListener Tests")
class WorkloadMessageListenerTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    private WorkloadMessageListener listener;

    private static final String DLQ_QUEUE = "workload.dlq";
    private static final String TRANSACTION_ID = "test-tx-123";
    private static final String DEFAULT_TX_ID = "Non-Provided";
    private static final String USERNAME = "john.doe";

    @BeforeEach
    void setUp() {
        listener = new WorkloadMessageListener(jmsTemplate, trainerWorkloadService);
        ReflectionTestUtils.setField(listener, "dlqQueue", DLQ_QUEUE);
    }

    private TrainerWorkloadDTO.Request.Create createValidRequest() {
        return new TrainerWorkloadDTO.Request.Create(
                USERNAME, "John", "Doe",
                TrainerStatus.ACTIVE,
                LocalDate.of(2025, 1, 15),
                2.5,
                ActionType.ADD
        );
    }

    @Nested
    @DisplayName("Valid Message Processing")
    class ValidMessageTests {

        @Test
        @DisplayName("Should process valid message successfully")
        void shouldProcessValidMessageSuccessfully() {
            var request = createValidRequest();

            listener.receiveMessage(request, TRANSACTION_ID);

            verify(trainerWorkloadService).obtainWorkload(eq(request), eq(TRANSACTION_ID));
            verify(jmsTemplate, never()).convertAndSend(anyString(), any(Object.class), any(MessagePostProcessor.class));
        }

        @Test
        @DisplayName("Should use default transactionId when null")
        void shouldUseDefaultTransactionIdWhenNull() {
            var request = createValidRequest();

            listener.receiveMessage(request, null);

            verify(trainerWorkloadService).obtainWorkload(eq(request), eq(DEFAULT_TX_ID));
        }

        @Test
        @DisplayName("Should use default transactionId when blank")
        void shouldUseDefaultTransactionIdWhenBlank() {
            var request = createValidRequest();

            listener.receiveMessage(request, "   ");

            verify(trainerWorkloadService).obtainWorkload(eq(request), eq(DEFAULT_TX_ID));
        }
    }

    @Nested
    @DisplayName("Invalid Message - Send to DLQ")
    class InvalidMessageTests {

        @Test
        @DisplayName("Should send to DLQ when username is null")
        void shouldSendToDlqWhenUsernameIsNull() {
            var request = new TrainerWorkloadDTO.Request.Create(
                    null, "John", "Doe",
                    TrainerStatus.ACTIVE,
                    LocalDate.of(2025, 1, 15),
                    2.5,
                    ActionType.ADD
            );

            listener.receiveMessage(request, TRANSACTION_ID);

            verify(jmsTemplate).convertAndSend(eq(DLQ_QUEUE), eq(request), any(MessagePostProcessor.class));
            verify(trainerWorkloadService, never()).obtainWorkload(any(), anyString());
        }

        @Test
        @DisplayName("Should send to DLQ when username is blank")
        void shouldSendToDlqWhenUsernameIsBlank() {
            var request = new TrainerWorkloadDTO.Request.Create(
                    "   ", "John", "Doe",
                    TrainerStatus.ACTIVE,
                    LocalDate.of(2025, 1, 15),
                    2.5,
                    ActionType.ADD
            );

            listener.receiveMessage(request, TRANSACTION_ID);

            verify(jmsTemplate).convertAndSend(eq(DLQ_QUEUE), eq(request), any(MessagePostProcessor.class));
            verify(trainerWorkloadService, never()).obtainWorkload(any(), anyString());
        }

        @Test
        @DisplayName("Should send to DLQ when actionType is null")
        void shouldSendToDlqWhenActionTypeIsNull() {
            var request = new TrainerWorkloadDTO.Request.Create(
                    USERNAME, "John", "Doe",
                    TrainerStatus.ACTIVE,
                    LocalDate.of(2025, 1, 15),
                    2.5,
                    null
            );

            listener.receiveMessage(request, TRANSACTION_ID);

            verify(jmsTemplate).convertAndSend(eq(DLQ_QUEUE), eq(request), any(MessagePostProcessor.class));
            verify(trainerWorkloadService, never()).obtainWorkload(any(), anyString());
        }

        @Test
        @DisplayName("Should send to DLQ when request is null")
        void shouldSendToDlqWhenRequestIsNull() {
            listener.receiveMessage(null, TRANSACTION_ID);

            verify(jmsTemplate).convertAndSend(eq(DLQ_QUEUE), isNull(), any(MessagePostProcessor.class));
            verify(trainerWorkloadService, never()).obtainWorkload(any(), anyString());
        }

        @Test
        @DisplayName("Should send to DLQ when trainingDuration is zero or negative")
        void shouldSendToDlqWhenTrainingDurationIsInvalid() {
            var request = new TrainerWorkloadDTO.Request.Create(
                    USERNAME, "John", "Doe",
                    TrainerStatus.ACTIVE,
                    LocalDate.of(2025, 1, 15),
                    0.0,
                    ActionType.ADD
            );

            listener.receiveMessage(request, TRANSACTION_ID);

            verify(jmsTemplate).convertAndSend(eq(DLQ_QUEUE), eq(request), any(MessagePostProcessor.class));
            verify(trainerWorkloadService, never()).obtainWorkload(any(), anyString());
        }
    }

    @Nested
    @DisplayName("Processing Error - Send to DLQ")
    class ProcessingErrorTests {

        @Test
        @DisplayName("Should send to DLQ when service throws exception")
        void shouldSendToDlqWhenServiceThrowsException() {
            var request = createValidRequest();
            doThrow(new RuntimeException("Database error"))
                    .when(trainerWorkloadService).obtainWorkload(any(), anyString());

            listener.receiveMessage(request, TRANSACTION_ID);

            verify(jmsTemplate).convertAndSend(eq(DLQ_QUEUE), eq(request), any(MessagePostProcessor.class));
        }
    }
}