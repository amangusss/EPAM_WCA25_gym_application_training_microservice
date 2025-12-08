package com.github.amangusss.exception;

public class TrainerNotFoundException extends BusinessException {
    public TrainerNotFoundException(String username) {
        super(String.format("Trainer not found: " + username), "TRAINER_NOT_FOUND");
    }
}
