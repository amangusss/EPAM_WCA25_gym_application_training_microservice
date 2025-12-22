package com.github.amangusss.entity;

import com.github.amangusss.converter.impl.TrainerStatusConverter;
import com.github.amangusss.converter.impl.YearMonthConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.YearMonth;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trainer_workloads",
       uniqueConstraints = @UniqueConstraint(columnNames = {"username", "period"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrainerWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "username", nullable = false)
    String username;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "last_name")
    String lastName;

    @Convert(converter = TrainerStatusConverter.class)
    TrainerStatus status;

    @Convert(converter = YearMonthConverter.class)
    @Column(name = "period", length = 7)
    YearMonth period;

    @Builder.Default
    @Column(name = "total_hours")
    Double totalHours = 0.0;
}
