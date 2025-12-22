package com.github.amangusss.jms;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.service.TrainerWorkloadService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkloadMessageListener {

    @Value("${app.jms.queue.workload-dlq}")
    String dlqQueue;

    final JmsTemplate jmsTemplate;
    final TrainerWorkloadService trainerWorkloadService1;

    @JmsListener(destination = "${app.jms.queue.workload}")
    public void receiveMessage(TrainerWorkloadDTO.Request.Create request,
                               @Header(name = "transactionId", required = false) String transactionId) {
        String effectiveTransactionId = (transactionId != null && !transactionId.isBlank())
                ? transactionId
                : "Non-Provided";
        log.info("[{}] Received workload message: username = {}", effectiveTransactionId, request.username());

        if (!isValidMessage(request)) {
            log.error("[{}] Invalid workload message: {}, sending to DLQ", effectiveTransactionId, request);
            sendToDLQ(request, effectiveTransactionId, "Required information is missing");
            return;
        }

        try {
            trainerWorkloadService1.obtainWorkload(request, effectiveTransactionId);
            log.info("[{}] Workload processed successfully", effectiveTransactionId);
        } catch (Exception e) {
            log.error("[{}] Error processing workload: {}", effectiveTransactionId, e.getMessage(), e);
            sendToDLQ(request, effectiveTransactionId, "Processing error: " + e.getMessage());
        }
    }

    private boolean isValidMessage(TrainerWorkloadDTO.Request.Create request) {
        if (request.username() != null && request.username().startsWith("DLQ_TEST_")) {
            return false;
        }

        return request != null
                && request.username() != null && !request.username().isBlank()
                && request.firstName() != null && !request.firstName().isBlank()
                && request.lastName() != null && !request.lastName().isBlank()
                && request.trainingDate() != null
                && request.trainingDuration() != null && request.trainingDuration() > 0
                && request.actionType() != null;
    }

    private void sendToDLQ(TrainerWorkloadDTO.Request.Create request, String transactionId, String reason) {
        try {
            jmsTemplate.convertAndSend(dlqQueue, request, message -> {
                message.setStringProperty("transactionId", transactionId);
                message.setStringProperty("errorReason", reason);
                message.setLongProperty("timeStamp", System.currentTimeMillis());
                return message;
            });
            log.info("[{}] Sent message to DLQ: {}", transactionId, request);
        } catch (Exception e) {
            log.error("[{}] Failed to send message to DLQ: {}", transactionId, request, e);
        }
    }
}
