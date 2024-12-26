package com.example.fitness_tracking.controller;


import com.example.fitness_tracking.model.ApiResponse;
import com.example.fitness_tracking.model.User;
import com.example.fitness_tracking.repository.UserRepository;
import com.example.fitness_tracking.validator.UserValidator;
import com.example.fitness_tracking.validator.ValidationResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Validated
@Slf4j
public class UserController {
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    @Autowired
    public UserController(UserRepository userRepository, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(@Valid @RequestBody User user) {
        try {
            ValidationResult validationResult = userValidator.validateNewUser(user);
            if (validationResult.isValid()) {
                return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse<>(false, validationResult.getErrors(), null));
            }

            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse<>(false, "Email is already in use", null));
            }

            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse<>(false, "Username is already taken", null));
            }

            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "User created successfully", savedUser));
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error creating user", null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable @Positive Long id) {
        try {
            return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new ApiResponse<>(true, "User found", user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found", null)));
        } catch (Exception e) {
            log.error("Error fetching user with id: {}", id, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching user", null));
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(
            @PathVariable @Email(message = "Invalid email format") String email) {
        try {
            return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(new ApiResponse<>(true, "User found", user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found", null)));
        } catch (Exception e) {
            log.error("Error fetching user with email: {}", email, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching user", null));
        }
    }

    @GetMapping("/fitness-level/{level}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByFitnessLevel(
            @PathVariable @NotNull String level) {
        try {
            User.FitnessLevel fitnessLevel = User.FitnessLevel.valueOf(level.toUpperCase());
            List<User> users = userRepository.findByFitnessLevel(fitnessLevel);
            return ResponseEntity.ok(new ApiResponse<>(true, "Users found", users));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(false, "Invalid fitness level", null));
        } catch (Exception e) {
            log.error("Error fetching users by fitness level: {}", level, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error fetching users", null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable @Positive Long id,
            @Valid @RequestBody User userDetails) {
        try {
            return (ResponseEntity<ApiResponse<User>>) userRepository.findById(id)
                .map(existingUser -> {
                    ValidationResult validationResult = userValidator.validateUpdateUser(userDetails);
                    if (validationResult.isValid()) {
                        return ResponseEntity
                            .badRequest()
                            .body(new ApiResponse<>(false, validationResult.getErrors(), null));
                    }

                    updateUserFields(existingUser, userDetails);
                    User updatedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", updatedUser));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found", null)));
        } catch (Exception e) {
            log.error("Error updating user with id: {}", id, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error updating user", null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable @Positive Long id) {
        try {
            return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found", null)));
        } catch (Exception e) {
            log.error("Error deleting user with id: {}", id, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error deleting user", null));
        }
    }

    @PatchMapping("/{id}/fitness-level")
    public ResponseEntity<ApiResponse<User>> updateFitnessLevel(
            @PathVariable @Positive Long id,
            @RequestParam @NotNull User.FitnessLevel fitnessLevel) {
        try {
            return userRepository.findById(id)
                .map(user -> {
                    user.setFitnessLevel(fitnessLevel);
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(new ApiResponse<>(true, "Fitness level updated successfully", updatedUser));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found", null)));
        } catch (Exception e) {
            log.error("Error updating fitness level for user with id: {}", id, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error updating fitness level", null));
        }
    }

    private void updateUserFields(User existingUser, User userDetails) {
        Optional.ofNullable(userDetails.getUsername()).ifPresent(existingUser::setUsername);
        Optional.ofNullable(userDetails.getEmail()).ifPresent(existingUser::setEmail);
        Optional.ofNullable(userDetails.getAge()).ifPresent(existingUser::setAge);
        Optional.ofNullable(userDetails.getHeight()).ifPresent(existingUser::setHeight);
        Optional.ofNullable(userDetails.getWeight()).ifPresent(existingUser::setWeight);
        Optional.ofNullable(userDetails.getGender()).ifPresent(existingUser::setGender);
        Optional.ofNullable(userDetails.getFitnessLevel()).ifPresent(existingUser::setFitnessLevel);
        Optional.ofNullable(userDetails.getPrimaryGoal()).ifPresent(existingUser::setPrimaryGoal);
        Optional.ofNullable(userDetails.getWeeklyWorkoutTarget()).ifPresent(existingUser::setWeeklyWorkoutTarget);
    }
}



