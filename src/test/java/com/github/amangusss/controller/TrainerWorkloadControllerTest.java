package com.github.amangusss.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.amangusss.dto.generated.TrainerSummaryResponse;
import com.github.amangusss.dto.trainerWorkload.TrainerWorkloadDTO;
import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.TrainerStatus;
import com.github.amangusss.mapper.GeneratedDtoMapper;
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

import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private GeneratedDtoMapper generatedDtoMapper;

    @InjectMocks
    private TrainerWorkloadController controller;

    private MockMvc mockMvc;

    private static final String USERNAME = "john.doe";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        ObjectMapper objectMapper = new ObjectMapper();
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

            TrainerSummaryResponse generatedResponse = new TrainerSummaryResponse();
            generatedResponse.setUsername(USERNAME);
            generatedResponse.setFirstName(FIRST_NAME);
            generatedResponse.setLastName(LAST_NAME);
            generatedResponse.setStatus(TrainerSummaryResponse.StatusEnum.ACTIVE);

            when(service.getTrainerSummary(eq(USERNAME), anyString())).thenReturn(summary);
            when(generatedDtoMapper.toGeneratedSummary(any())).thenReturn(generatedResponse);

            mockMvc.perform(get("/api/v1/workload/{username}", USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                    .andExpect(jsonPath("$.lastName").value(LAST_NAME))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(service).getTrainerSummary(eq(USERNAME), anyString());
            verify(generatedDtoMapper).toGeneratedSummary(any());
        }

        @Test
        @DisplayName("Should use provided transaction ID from header")
        void shouldUseProvidedTransactionId() throws Exception {
            String transactionId = "custom-transaction-id";
            var summary = new TrainerWorkloadDTO.Response.Summary(
                    USERNAME, FIRST_NAME, LAST_NAME,
                    TrainerStatus.ACTIVE, List.of()
            );

            TrainerSummaryResponse generatedResponse = new TrainerSummaryResponse();
            generatedResponse.setUsername(USERNAME);
            generatedResponse.setFirstName(FIRST_NAME);
            generatedResponse.setLastName(LAST_NAME);
            generatedResponse.setStatus(TrainerSummaryResponse.StatusEnum.ACTIVE);

            when(service.getTrainerSummary(eq(USERNAME), eq(transactionId))).thenReturn(summary);
            when(generatedDtoMapper.toGeneratedSummary(any())).thenReturn(generatedResponse);

            mockMvc.perform(get("/api/v1/workload/{username}", USERNAME)
                            .header("X-transaction-id", transactionId))
                    .andExpect(status().isOk());

            verify(service).getTrainerSummary(eq(USERNAME), eq(transactionId));
            verify(generatedDtoMapper).toGeneratedSummary(any());
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

            TrainerSummaryResponse generatedResponse = new TrainerSummaryResponse();
            generatedResponse.setUsername(USERNAME);
            generatedResponse.setFirstName(FIRST_NAME);
            generatedResponse.setLastName(LAST_NAME);
            generatedResponse.setStatus(TrainerSummaryResponse.StatusEnum.ACTIVE);

            when(service.getTrainerSummary(eq(USERNAME), anyString())).thenReturn(summary);
            when(generatedDtoMapper.toGeneratedSummary(any())).thenReturn(generatedResponse);

            mockMvc.perform(get("/api/v1/workload/{username}", USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                    .andExpect(jsonPath("$.lastName").value(LAST_NAME));

            verify(generatedDtoMapper).toGeneratedSummary(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/workload")
    class ProcessTrainingTests {

        @Test
        @DisplayName("Should process training event and return 200 OK")
        void shouldProcessTrainingEventAndReturnOk() throws Exception {
            String requestJson = """
                    {
                        "username": "john.doe",
                        "firstName": "John",
                        "lastName": "Doe",
                        "status": "ACTIVE",
                        "trainingDate": "2025-01-15T00:00:00.000Z",
                        "trainingDuration": 2.5,
                        "actionType": "ADD"
                    }
                    """;

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/workload")
                            .contentType("application/json")
                            .content(requestJson))
                    .andExpect(status().isOk());

            verify(generatedDtoMapper).toInternalCreate(any());
            verify(service).obtainWorkload(any(), anyString());
        }

        @Test
        @DisplayName("Should use custom transaction ID from header")
        void shouldUseCustomTransactionIdFromHeader() throws Exception {
            String customTransactionId = "test-txn-123";
            String requestJson = """
                    {
                        "username": "john.doe",
                        "firstName": "John",
                        "lastName": "Doe",
                        "status": "ACTIVE",
                        "trainingDate": "2025-01-15T00:00:00.000Z",
                        "trainingDuration": 2.5,
                        "actionType": "ADD"
                    }
                    """;

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/workload")
                            .header("X-Transaction-Id", customTransactionId)
                            .contentType("application/json")
                            .content(requestJson))
                    .andExpect(status().isOk());

            verify(service).obtainWorkload(any(), eq(customTransactionId));
        }

        @Test
        @DisplayName("Should process DELETE action type")
        void shouldProcessDeleteActionType() throws Exception {
            String requestJson = """
                    {
                        "username": "john.doe",
                        "firstName": "John",
                        "lastName": "Doe",
                        "status": "ACTIVE",
                        "trainingDate": "2025-01-15T00:00:00.000Z",
                        "trainingDuration": 2.5,
                        "actionType": "DELETE"
                    }
                    """;

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/workload")
                            .contentType("application/json")
                            .content(requestJson))
                    .andExpect(status().isOk());

            verify(generatedDtoMapper).toInternalCreate(any());
            verify(service).obtainWorkload(any(), anyString());
        }

        @Test
        @DisplayName("Should handle INACTIVE status")
        void shouldHandleInactiveStatus() throws Exception {
            String requestJson = """
                    {
                        "username": "john.doe",
                        "firstName": "John",
                        "lastName": "Doe",
                        "status": "INACTIVE",
                        "trainingDate": "2025-01-15T00:00:00.000Z",
                        "trainingDuration": 2.5,
                        "actionType": "ADD"
                    }
                    """;

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/workload")
                            .contentType("application/json")
                            .content(requestJson))
                    .andExpect(status().isOk());

            verify(service).obtainWorkload(any(), anyString());
        }
    }
}