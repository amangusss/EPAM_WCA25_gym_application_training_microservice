package com.github.amangusss.repository;

import com.github.amangusss.entity.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {
    Optional<TrainerWorkload> findByUsernameAndPeriod(String username, YearMonth period);
    boolean existsByUsernameAndPeriod(String username, YearMonth period);
    List<TrainerWorkload> findByUsername(String username);
}
