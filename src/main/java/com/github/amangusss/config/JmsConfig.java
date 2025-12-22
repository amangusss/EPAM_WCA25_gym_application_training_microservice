package com.github.amangusss.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

@Configuration
@EnableJms
public class JmsConfig {

    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);

        Map<String, Class<?>> typeIdMappings = Map.of(
                "com.github.amangusss.gym_application.dto.workload.WorkloadDTO$Request$Workload",
                TrainerWorkloadDTO.Request.Create.class
        );
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }
}
