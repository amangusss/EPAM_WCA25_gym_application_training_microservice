package com.github.amangusss.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public abstract class AbstractCodedEnumConverter<E extends Enum<E> & CodedEnum> implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    protected AbstractCodedEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return EnumConverter.toCode(attribute);
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return EnumConverter.fromCode(enumClass, dbData);
    }
}
