package com.github.amangusss.cucumber.steps;

import com.github.amangusss.entity.Month;
import com.github.amangusss.entity.MonthSummary;
import com.github.amangusss.entity.TrainerStatus;
import com.github.amangusss.entity.TrainerWorkload;
import com.github.amangusss.entity.YearSummary;
import com.github.amangusss.repository.TrainerWorkloadRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SuppressWarnings("unused")
public class TrainerSummarySteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private TrainerWorkloadSteps sharedSteps;

    @Given("trainer {string} exists with following workload:")
    public void trainerExistsWithFollowingWorkload(String username, DataTable dataTable) {
        TrainerWorkload workload = new TrainerWorkload();
        workload.setUsername(username);
        workload.setFirstName("John");
        workload.setLastName("Smith");
        workload.setStatus(TrainerStatus.ACTIVE);

        List<YearSummary> years = new ArrayList<>();
        List<Map<String, String>> rows = dataTable.asMaps();

        for (Map<String, String> row : rows) {
            int year = Integer.parseInt(row.get("year"));
            Month month = Month.valueOf(row.get("month"));
            double hours = Double.parseDouble(row.get("hours"));

            YearSummary yearSummary = years.stream()
                    .filter(y -> y.getYear() == year)
                    .findFirst()
                    .orElseGet(() -> {
                        YearSummary newYear = new YearSummary();
                        newYear.setYear(year);
                        newYear.setMonths(new ArrayList<>());
                        years.add(newYear);
                        return newYear;
                    });

            MonthSummary monthSummary = new MonthSummary();
            monthSummary.setMonth(month);
            monthSummary.setTotalHours(hours);
            yearSummary.getMonths().add(monthSummary);
        }

        workload.setYears(years);
        repository.save(workload);
    }

    @Given("trainer {string} exists with no workload")
    public void trainerExistsWithNoWorkload(String username) {
        TrainerWorkload workload = new TrainerWorkload();
        workload.setUsername(username);
        workload.setFirstName("Jane");
        workload.setLastName("Doe");
        workload.setStatus(TrainerStatus.ACTIVE);
        workload.setYears(new ArrayList<>());
        repository.save(workload);
    }

    @Given("trainer {string} exists with status {string} and {double} hours for January {int}")
    public void trainerExistsWithStatusAndHoursForJanuary(String username, String status, double hours, int year) {
        TrainerWorkload workload = new TrainerWorkload();
        workload.setUsername(username);
        workload.setFirstName("John");
        workload.setLastName("Smith");
        workload.setStatus(TrainerStatus.valueOf(status));

        YearSummary yearSummary = new YearSummary();
        yearSummary.setYear(year);
        MonthSummary monthSummary = new MonthSummary();
        monthSummary.setMonth(Month.JANUARY);
        monthSummary.setTotalHours(hours);
        yearSummary.setMonths(List.of(monthSummary));
        workload.setYears(List.of(yearSummary));

        repository.save(workload);
    }

    @When("I request summary for trainer {string}")
    public void iRequestSummaryForTrainer(String username) throws Exception {
        sharedSteps.resultActions = mockMvc.perform(get("/api/v1/workload/{username}", username));
    }

    @Then("the summary should contain username {string}")
    public void theSummaryShouldContainUsername(String username) throws Exception {
        sharedSteps.resultActions.andExpect(jsonPath("$.username").value(username));
    }

    @And("the summary should contain {int} months of data")
    public void theSummaryShouldContainMonthsOfData(int monthCount) throws Exception {
        if (monthCount == 0) {
            sharedSteps.resultActions.andExpect(jsonPath("$.years").isEmpty());
        } else {
            sharedSteps.resultActions.andExpect(jsonPath("$.years[0].months", hasSize(monthCount)));
        }
    }

    @And("the summary should be empty")
    public void theSummaryShouldBeEmpty() throws Exception {
        sharedSteps.resultActions.andExpect(status().isOk());
    }

    @And("the summary should contain status {string}")
    public void theSummaryShouldContainStatus(String status) throws Exception {
        sharedSteps.resultActions.andExpect(jsonPath("$.status").value(status));
    }
}
