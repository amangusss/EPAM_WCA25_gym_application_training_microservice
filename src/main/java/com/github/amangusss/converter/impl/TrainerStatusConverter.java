package com.github.amangusss.converter.impl;

import com.github.amangusss.converter.AbstractCodedEnumConverter;
import com.github.amangusss.entity.TrainerStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TrainerStatusConverter extends AbstractCodedEnumConverter<TrainerStatus> {
    public TrainerStatusConverter() {
        super(TrainerStatus.class);
    }
}
