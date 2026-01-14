package com.github.amangusss;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class TrainerWorkloadMicroservice {
    public static void main(String[] args) {
        SpringApplication.run(TrainerWorkloadMicroservice.class, args);
        log.info("Microservice started successfully. REST API is available.");
    }
}
