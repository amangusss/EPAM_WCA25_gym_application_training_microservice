package com.github.amangusss.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trainer_workloads")
@CompoundIndex(name = "username_period_idx", def = "{'username': 1, 'period': 1}", unique = true)
@CompoundIndex(name = "name_idx", def = "{'firstName': 1, 'lastName': 1}")
public class TrainerWorkload {

    @Id
    private String id;

    @Indexed
    @Field("username")
    private String username;

    @Field("firstName")
    private String firstName;

    @Field("lastName")
    private String lastName;

    @Field("period")
    private YearMonth period;

    @Builder.Default
    @Field("totalHours")
    private Double totalHours = 0.0;

    @Builder.Default
    @Field("status")
    private TrainerStatus status = TrainerStatus.ACTIVE;
}
