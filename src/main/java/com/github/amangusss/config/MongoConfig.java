package com.github.amangusss.config;

import com.github.amangusss.converter.impl.YearMonthConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.YearMonth;
import java.util.Arrays;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new YearMonthToStringConverter(),
                new StringToYearMonthConverter()
        ));
    }

    private static class YearMonthToStringConverter implements org.springframework.core.convert.converter.Converter<YearMonth, String> {
        private final YearMonthConverter converter = new YearMonthConverter();

        @Override
        public String convert(YearMonth source) {
            return converter.convertToDatabaseColumn(source);
        }
    }

    private static class StringToYearMonthConverter implements org.springframework.core.convert.converter.Converter<String, YearMonth> {
        private final YearMonthConverter converter = new YearMonthConverter();

        @Override
        public YearMonth convert(String source) {
            return converter.convertToEntityAttribute(source);
        }
    }
}

