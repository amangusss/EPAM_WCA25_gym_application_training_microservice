package com.github.amangusss.entity;

import com.github.amangusss.converter.CodedEnum;
import lombok.Getter;

@Getter
public enum ActionType implements CodedEnum {
    ADD("add"),
    DELETE("delete");

    private final String code;

    ActionType(String code) {
        this.code = code;
    }
}
