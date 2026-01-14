package com.github.amangusss.mapper;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.MonthSummary;
import com.github.amangusss.entity.TrainerWorkload;

import com.github.amangusss.entity.YearSummary;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class TrainerWorkloadMapper {

    public TrainerWorkloadDTO.Response.Summary toSummary(TrainerWorkload workload) {
        if (workload == null) {
            return null;
        }

        List<TrainerWorkloadDTO.YearSummary> years = workload.getYears().stream()
                .map(this::toYearSummaryDto)
                .sorted(Comparator.comparingInt(TrainerWorkloadDTO.YearSummary::year))
                .toList();

        return new TrainerWorkloadDTO.Response.Summary(
                workload.getUsername(),
                workload.getFirstName(),
                workload.getLastName(),
                workload.getStatus(),
                years
        );
    }

    public void updateWorkloadInfo(TrainerWorkload workload, TrainerWorkloadDTO.Request.Create request) {
        workload.setFirstName(request.firstName());
        workload.setLastName(request.lastName());
        workload.setStatus(request.status());
    }

    private TrainerWorkloadDTO.YearSummary toYearSummaryDto(YearSummary yearSummary) {
        List<TrainerWorkloadDTO.MonthSummary> months = yearSummary.getMonths().stream()
                .map(this::toMonthSummaryDto)
                .toList();
        return new TrainerWorkloadDTO.YearSummary(yearSummary.getYear(), months);
    }

    private TrainerWorkloadDTO.MonthSummary toMonthSummaryDto(MonthSummary monthSummary) {
        return new TrainerWorkloadDTO.MonthSummary(
                monthSummary.getMonth(),
                monthSummary.getTotalHours()
        );
    }
}
