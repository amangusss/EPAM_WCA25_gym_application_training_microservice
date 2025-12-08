package com.github.amangusss.mapper;

import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.TrainerWorkload;

import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrainerWorkloadMapper {

    public TrainerWorkload toEntity(TrainerWorkloadDTO.Request.Create request, YearMonth period) {
        return TrainerWorkload.builder()
                .username(request.username())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .status(request.status())
                .period(period)
                .totalHours(request.trainingDuration())
                .build();
    }

    public TrainerWorkloadDTO.Response.Summary toSummary(List<TrainerWorkload> workloads) {
        if (workloads == null || workloads.isEmpty()) {
            return null;
        }

        TrainerWorkload first = workloads.get(0);

        List<TrainerWorkloadDTO.YearSummary> years = workloads.stream()
                .collect(Collectors.groupingBy(
                        w -> w.getPeriod().getYear(),
                        Collectors.mapping(
                                this::toMonthSummary,
                                Collectors.toList()
                        )
                ))
                .entrySet().stream()
                .map(e -> new TrainerWorkloadDTO.YearSummary(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(TrainerWorkloadDTO.YearSummary::year))
                .toList();

        return new TrainerWorkloadDTO.Response.Summary(
                first.getUsername(),
                first.getFirstName(),
                first.getLastName(),
                first.getStatus(),
                years
        );
    }

    public TrainerWorkloadDTO.MonthSummary toMonthSummary(TrainerWorkload workload) {
        return new TrainerWorkloadDTO.MonthSummary(
                Month.of(workload.getPeriod().getMonthValue()),
                workload.getTotalHours()
        );
    }

    public void updateEntityFromRequest(TrainerWorkload workload, TrainerWorkloadDTO.Request.Create request) {
        workload.setFirstName(request.firstName());
        workload.setLastName(request.lastName());
        workload.setStatus(request.status());
    }
}
