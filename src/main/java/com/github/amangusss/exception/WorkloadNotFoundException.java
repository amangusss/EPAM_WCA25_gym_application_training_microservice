package com.github.amangusss.exception;

import java.time.YearMonth;

public class WorkloadNotFoundException extends BusinessException {
    public WorkloadNotFoundException(String username, YearMonth period) {
        super(String.format("Workload not found for trainer: " + username + " for period: " + period), "WORKLOAD_NOT_FOUND");
    }
}
