package com.github.amangusss.entity;

import com.github.amangusss.converter.CodedEnum;
import lombok.Getter;

@Getter(onMethod_ = {@Override})
public enum TrainerStatus implements CodedEnum {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String code;

    TrainerStatus(String code) {
        this.code = code;
    }
}
