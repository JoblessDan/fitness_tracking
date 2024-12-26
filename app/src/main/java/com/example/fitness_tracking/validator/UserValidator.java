package com.example.fitness_tracking.validator;

import com.example.fitness_tracking.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {
    public ValidationResult validateNewUser(User user) {
        ValidationResult result = new ValidationResult();

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            result.addError("Username is required");
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            result.addError("Valid email is required");
        }
        if (user.getAge() != null && (user.getAge() < 13 || user.getAge() > 120)) {
            result.addError("Age must be between 13 and 120");
        }

        return result;
    }

    public ValidationResult validateUpdateUser(User user) {
        ValidationResult result = new ValidationResult();

        if (user.getEmail() != null && !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            result.addError("Invalid email format");
        }
        if (user.getAge() != null && (user.getAge() < 13 || user.getAge() > 120)) {
            result.addError("Age must be between 13 and 120");
        }

        return result;
    }
}

