package com.github.amangusss.config;

import com.github.amangusss.converter.CodedEnum;
import com.github.amangusss.converter.jackson.CodedEnumDeserializer;
import com.github.amangusss.converter.jackson.CodedEnumSerializer;
import com.github.amangusss.converter.jackson.TrainerStatusDeserializer;
import com.github.amangusss.entity.ActionType;
import com.github.amangusss.entity.TrainerStatus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule codedEnumModule() {
        SimpleModule module = new SimpleModule("CodedEnumModule");

        registerTrainerStatus(module);
        registerCodedEnum(module, ActionType.class);

        return module;
    }

    private <E extends Enum<E> & CodedEnum> void registerCodedEnum(SimpleModule module, Class<E> enumClass) {
        module.addSerializer(enumClass, new CodedEnumSerializer<>(enumClass));
        module.addDeserializer(enumClass, new CodedEnumDeserializer<>(enumClass));
    }

    private void registerTrainerStatus(SimpleModule module) {
        module.addSerializer(TrainerStatus.class, new CodedEnumSerializer<>(TrainerStatus.class));
        module.addDeserializer(TrainerStatus.class, new TrainerStatusDeserializer());
    }
}
