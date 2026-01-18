package com.github.amangusss.cucumber.steps;

import com.github.amangusss.repository.TrainerWorkloadRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class Hooks {

    @Autowired
    private TrainerWorkloadRepository repository;

    @Before
    public void setUp() {
        repository.deleteAll();
    }

    @After
    public void tearDown() {
        repository.deleteAll();
    }
}
