package com.example.fitness_tracking.validator;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ValidationResult {
    private final List<String> errors = new ArrayList<>();

    public void addError(String error) {
        errors.add(error);
    }

    public boolean isValid() {
        return !errors.isEmpty();
    }

    public String getErrors() {
        return String.join(", ", errors);
    }
}
