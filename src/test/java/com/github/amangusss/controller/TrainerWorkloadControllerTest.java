package com.github.amangusss.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.TrainerStatus;
import com.github.amangusss.service.TrainerWorkloadService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerWorkloadController Tests")
class TrainerWorkloadControllerTest {

    @Mock
    private TrainerWorkloadService service;

    @InjectMocks
    private TrainerWorkloadController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final String USERNAME = "john.doe";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("GET /api/v1/workload/{username}")
    class GetTrainerSummaryTests {

        @Test
        @DisplayName("Should return 200 OK with trainer summary")
        void shouldReturnOkWithTrainerSummary() throws Exception {
            var summary = new TrainerWorkloadDTO.Response.Summary(
                    USERNAME, FIRST_NAME, LAST_NAME,
                    TrainerStatus.ACTIVE,
                    List.of(new TrainerWorkloadDTO.YearSummary(2025, List.of(
                            new TrainerWorkloadDTO.MonthSummary(Month.of(1), 5.0)
                    )))
            );
            when(service.getTrainerSummary(eq(USERNAME), anyString())).thenReturn(summary);

            mockMvc.perform(get("/api/v1/workload/{username}", USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                    .andExpect(jsonPath("$.lastName").value(LAST_NAME))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.years[0].year").value(2025))
                    .andExpect(jsonPath("$.years[0].months[0].month").value("JANUARY"))
                    .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration").value(5.0));

            verify(service).getTrainerSummary(eq(USERNAME), anyString());
        }

        @Test
        @DisplayName("Should use provided transaction ID from header")
        void shouldUseProvidedTransactionId() throws Exception {
            String transactionId = "custom-transaction-id";
            var summary = new TrainerWorkloadDTO.Response.Summary(
                    USERNAME, FIRST_NAME, LAST_NAME,
                    TrainerStatus.ACTIVE, List.of()
            );
            when(service.getTrainerSummary(eq(USERNAME), eq(transactionId))).thenReturn(summary);

            mockMvc.perform(get("/api/v1/workload/{username}", USERNAME)
                            .header("X-transaction-id", transactionId))
                    .andExpect(status().isOk());

            verify(service).getTrainerSummary(eq(USERNAME), eq(transactionId));
        }

        @Test
        @DisplayName("Should return summary with multiple years and months")
        void shouldReturnSummaryWithMultipleYearsAndMonths() throws Exception {
            var summary = new TrainerWorkloadDTO.Response.Summary(
                    USERNAME, FIRST_NAME, LAST_NAME,
                    TrainerStatus.ACTIVE,
                    List.of(
                            new TrainerWorkloadDTO.YearSummary(2024, List.of(
                                    new TrainerWorkloadDTO.MonthSummary(Month.of(11), 3.0),
                                    new TrainerWorkloadDTO.MonthSummary(Month.of(12), 4.0)
                            )),
                            new TrainerWorkloadDTO.YearSummary(2025, List.of(
                                    new TrainerWorkloadDTO.MonthSummary(Month.of(1), 5.0)
                            ))
                    )
            );
            when(service.getTrainerSummary(eq(USERNAME), anyString())).thenReturn(summary);

            mockMvc.perform(get("/api/v1/workload/{username}", USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.years").isArray())
                    .andExpect(jsonPath("$.years.length()").value(2))
                    .andExpect(jsonPath("$.years[0].year").value(2024))
                    .andExpect(jsonPath("$.years[0].months.length()").value(2))
                    .andExpect(jsonPath("$.years[1].year").value(2025));
        }
    }
}