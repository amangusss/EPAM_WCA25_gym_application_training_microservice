package com.github.amangusss.converter.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.amangusss.converter.CodedEnum;
import com.github.amangusss.converter.EnumConverter;

import java.io.IOException;

public class CodedEnumDeserializer<E extends Enum<E> & CodedEnum> extends JsonDeserializer<E> {

    private final Class<E> enumClass;

    public CodedEnumDeserializer(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public E deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return EnumConverter.fromCode(enumClass, parser.getValueAsString());
    }
}
