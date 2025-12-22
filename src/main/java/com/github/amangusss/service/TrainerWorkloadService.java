package com.github.amangusss.service;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;

public interface TrainerWorkloadService {
    void obtainWorkload(TrainerWorkloadDTO.Request.Create request, String transactionId);
    TrainerWorkloadDTO.Response.Summary getTrainerSummary(String username, String transactionId);
}
