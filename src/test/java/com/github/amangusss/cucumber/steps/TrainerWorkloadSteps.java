package com.github.amangusss.cucumber.steps;

import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.MonthSummary;
import com.github.amangusss.entity.TrainerStatus;
import com.github.amangusss.entity.TrainerWorkload;
import com.github.amangusss.entity.YearSummary;
import com.github.amangusss.repository.TrainerWorkloadRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrainerWorkloadSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainerWorkloadRepository repository;

    public ResultActions resultActions;

    @Given("trainer {string} does not exist in the system")
    public void trainerDoesNotExistInTheSystem(String username) {
        repository.findByUsername(username).ifPresent(repository::delete);
    }

    @Given("trainer {string} exists with {double} hours for January {int}")
    public void trainerExistsWithHoursForJanuary(String username, double hours, int year) {
        TrainerWorkload workload = createWorkload(username, "John", "Smith", TrainerStatus.ACTIVE);
        YearSummary yearSummary = new YearSummary();
        yearSummary.setYear(year);
        MonthSummary monthSummary = new MonthSummary();
        monthSummary.setMonth(Month.JANUARY);
        monthSummary.setTotalHours(hours);
        yearSummary.setMonths(List.of(monthSummary));
        workload.setYears(List.of(yearSummary));
        repository.save(workload);
    }

    @Given("trainer {string} exists with {double} hours for February {int}")
    public void trainerExistsWithHoursForFebruary(String username, double hours, int year) {
        Optional<TrainerWorkload> existingWorkload = repository.findByUsername(username);
        TrainerWorkload workload;

        if (existingWorkload.isPresent()) {
            workload = existingWorkload.get();
            YearSummary yearSummary = workload.getYears().stream()
                    .filter(y -> y.getYear() == year)
                    .findFirst()
                    .orElseGet(() -> {
                        YearSummary newYear = new YearSummary();
                        newYear.setYear(year);
                        newYear.setMonths(new ArrayList<>());
                        workload.getYears().add(newYear);
                        return newYear;
                    });

            MonthSummary monthSummary = new MonthSummary();
            monthSummary.setMonth(Month.FEBRUARY);
            monthSummary.setTotalHours(hours);
            yearSummary.getMonths().add(monthSummary);
        } else {
            workload = createWorkload(username, "John", "Smith", TrainerStatus.ACTIVE);
            YearSummary yearSummary = new YearSummary();
            yearSummary.setYear(year);
            MonthSummary monthSummary = new MonthSummary();
            monthSummary.setMonth(Month.FEBRUARY);
            monthSummary.setTotalHours(hours);
            yearSummary.setMonths(List.of(monthSummary));
            workload.setYears(List.of(yearSummary));
        }

        repository.save(workload);
    }

    @When("I send ADD workload request for trainer {string} with {double} hours on {string}")
    public void iSendAddWorkloadRequestForTrainerWithHoursOn(String username, double hours, String date) throws Exception {
        String requestJson = """
                {
                    "username": "%s",
                    "firstName": "John",
                    "lastName": "Smith",
                    "status": "ACTIVE",
                    "trainingDate": "%s",
                    "trainingDuration": %s,
                    "actionType": "ADD"
                }
                """.formatted(username, date, hours);

        resultActions = mockMvc.perform(post("/api/v1/workload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));
    }

    @When("I send DELETE workload request for trainer {string} with {double} hours on {string}")
    public void iSendDeleteWorkloadRequestForTrainerWithHoursOn(String username, double hours, String date) throws Exception {
        String requestJson = """
                {
                    "username": "%s",
                    "firstName": "John",
                    "lastName": "Smith",
                    "status": "ACTIVE",
                    "trainingDate": "%s",
                    "trainingDuration": %s,
                    "actionType": "DELETE"
                }
                """.formatted(username, date, hours);

        resultActions = mockMvc.perform(post("/api/v1/workload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));
    }

    @When("I send ADD workload request with negative hours {double}")
    public void iSendAddWorkloadRequestWithNegativeHours(double hours) throws Exception {
        String requestJson = """
                {
                    "username": "john.smith",
                    "firstName": "John",
                    "lastName": "Smith",
                    "status": "ACTIVE",
                    "trainingDate": "2025-01-15",
                    "trainingDuration": %s,
                    "actionType": "ADD"
                }
                """.formatted(hours);

        resultActions = mockMvc.perform(post("/api/v1/workload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));
    }

    @When("I send ADD workload request without username")
    public void iSendAddWorkloadRequestWithoutUsername() throws Exception {
        String requestJson = """
                {
                    "firstName": "John",
                    "lastName": "Smith",
                    "status": "ACTIVE",
                    "trainingDate": "2025-01-15",
                    "trainingDuration": 2.5,
                    "actionType": "ADD"
                }
                """;

        resultActions = mockMvc.perform(post("/api/v1/workload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int statusCode) throws Exception {
        resultActions.andExpect(status().is(statusCode));
    }

    @And("trainer {string} should have {double} hours for January {int}")
    public void trainerShouldHaveHoursForJanuary(String username, double expectedHours, int year) {
        TrainerWorkload workload = repository.findByUsername(username).orElseThrow();
        double actualHours = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth() == Month.JANUARY)
                .mapToDouble(MonthSummary::getTotalHours)
                .sum();

        assertThat(actualHours).isEqualTo(expectedHours);
    }

    @And("trainer {string} should have {double} hours for February {int}")
    public void trainerShouldHaveHoursForFebruary(String username, double expectedHours, int year) {
        TrainerWorkload workload = repository.findByUsername(username).orElseThrow();
        double actualHours = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth() == Month.FEBRUARY)
                .mapToDouble(MonthSummary::getTotalHours)
                .sum();

        assertThat(actualHours).isEqualTo(expectedHours);
    }

    private TrainerWorkload createWorkload(String username, String firstName, String lastName, TrainerStatus status) {
        TrainerWorkload workload = new TrainerWorkload();
        workload.setUsername(username);
        workload.setFirstName(firstName);
        workload.setLastName(lastName);
        workload.setStatus(status);
        workload.setYears(new ArrayList<>());
        return workload;
    }
}
