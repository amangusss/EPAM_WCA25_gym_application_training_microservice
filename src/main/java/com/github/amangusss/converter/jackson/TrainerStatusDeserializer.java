package com.github.amangusss.converter.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.amangusss.entity.TrainerStatus;

import java.io.IOException;

public class TrainerStatusDeserializer extends JsonDeserializer<TrainerStatus> {

    @Override
    public TrainerStatus deserialize(JsonParser parser, DeserializationContext context) throws IOException {

        if (parser.currentToken() == JsonToken.VALUE_TRUE) {
            return TrainerStatus.ACTIVE;
        }
        if (parser.currentToken() == JsonToken.VALUE_FALSE) {
            return TrainerStatus.INACTIVE;
        }

        String value = parser.getValueAsString();
        if (value != null) {
            return switch (value.toLowerCase()) {
                case "active", "true" -> TrainerStatus.ACTIVE;
                case "inactive", "false" -> TrainerStatus.INACTIVE;
                default -> throw context.weirdStringException(value, TrainerStatus.class, "Unknown value");
            };
        }

        return null;
    }
}
