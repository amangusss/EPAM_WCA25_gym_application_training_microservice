package com.github.amangusss.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearSummary {

    @Field("year")
    private Integer year;

    @Builder.Default
    @Field("months")
    private List<MonthSummary> months = new ArrayList<>();
}
