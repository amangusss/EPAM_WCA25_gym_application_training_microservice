package com.github.amangusss.entity;

import com.github.amangusss.converter.CodedEnum;

public enum TrainerStatus implements CodedEnum {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String code;

    TrainerStatus(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    public static TrainerStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return ACTIVE;
        }
        for (TrainerStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown TrainerStatus code: " + code);
    }
}
