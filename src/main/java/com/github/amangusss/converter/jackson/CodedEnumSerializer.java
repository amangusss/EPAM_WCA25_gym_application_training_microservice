package com.github.amangusss.converter.jackson;

import com.github.amangusss.converter.CodedEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CodedEnumSerializer<E extends Enum<E> & CodedEnum> extends JsonSerializer<E> {

    private final Class<E> enumClass;

    public CodedEnumSerializer(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public void serialize(E value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(value.getCode());
        }
    }

    @Override
    public Class<E> handledType() {
        return enumClass;
    }
}
