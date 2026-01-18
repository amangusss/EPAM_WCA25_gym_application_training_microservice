package com.github.amangusss.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthSummary {

    @Field("month")
    private Month month;

    @Builder.Default
    @Field("totalHours")
    private Double totalHours = 0.0;
}
