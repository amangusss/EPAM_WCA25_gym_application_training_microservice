package com.github.amangusss.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Month {
    JANUARY(1),
    FEBRUARY(2),
    MARCH(3),
    APRIL(4),
    MAY(5),
    JUNE(6),
    JULY(7),
    AUGUST(8),
    SEPTEMBER(9),
    OCTOBER(10),
    NOVEMBER(11),
    DECEMBER(12);

    private final int value;

    Month(int value) {
        this.value = value;
    }

    @JsonValue
    public String toJson() {
        return name();
    }

    public static Month of(int month) {
        for (Month m : values()) {
            if (m.value == month) {
                return m;
            }
        }
        throw new IllegalArgumentException("Invalid month: " + month);
    }
}

