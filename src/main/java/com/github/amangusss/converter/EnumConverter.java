package com.github.amangusss.converter;

import com.github.amangusss.exception.UnsupportedCodeException;

import java.util.Arrays;

public final class EnumConverter {

    private EnumConverter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <E extends Enum<E> & CodedEnum> E fromCode(Class<E> enumClass, String code) {
        if (code == null) {
            return null;
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new UnsupportedCodeException(code, enumClass.getSimpleName())
                );
    }

    public static <E extends Enum<E> & CodedEnum> String toCode(E enumValue) {
        return enumValue != null ? enumValue.getCode() : null;
    }
}
