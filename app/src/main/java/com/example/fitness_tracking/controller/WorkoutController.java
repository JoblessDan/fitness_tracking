package com.example.fitness_tracking.controller;

import com.example.fitness_tracking.model.User;
import com.example.fitness_tracking.model.Workout;
import com.example.fitness_tracking.repository.UserRepository;
import com.example.fitness_tracking.repository.WorkoutRepository;
import com.example.fitness_tracking.service.WorkoutAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * The WorkoutController class provides a RESTful API for managing workouts.
 *
 * This controller handles HTTP requests to perform CRUD operations on workouts,
 * retrieve workouts by various criteria, and access analytics and machine learning
 * data for a user's workout history. The endpoints are secured to ensure proper
 * user handling and return appropriate HTTP responses based on the outcome.
 *
 * Key operations provided by this controller include:
 * - Creating, retrieving, updating, and deleting workouts.
 * - Retrieving workouts filtered by type, date range, and user-specific criteria.
 * - Generating workout analytics and preparing machine learning data.
 *
 * The controller interacts with the {@code UserRepository}, {@code WorkoutRepository},
 * and {@code WorkoutAnalyticsService} to perform these operations.
 */
@RestController
@RequestMapping("/api/workouts")
@CrossOrigin(origins = "*")
public class WorkoutController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private WorkoutAnalyticsService analyticsService;

    @PostMapping
public ResponseEntity<?> createWorkout(@RequestBody Workout workout) {
        try {
            if (workout.getUser() != null && workout.getUser().getId() != null) {
                User user = userRepository.findById(workout.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                workout.setUser(user);
            }

            if (workout.getTimestamp() == null) {
                workout.setTimestamp(LocalDateTime.now());
            }

            Workout savedWorkout = workoutRepository.save(workout);
            return ResponseEntity.ok(savedWorkout);
        } catch (Exception e) {
            return ResponseEntity
                .badRequest()
                .body("Error creating workout: " + e.getMessage());
        }
    }

    /**
     * Retrieves a workout by its unique identifier.
     *
     * This method handles HTTP GET requests to fetch a single workout resource
     * based on the provided ID. If a workout with the specified ID exists,
     * it is returned with a 200 OK response. Otherwise, a 404 Not Found response
     * is returned.
     *
     * @param id the unique identifier of the workout to be retrieved
     * @return a response containing the workout if found, or a 404 Not Found response if no workout exists with the specified ID
     */

    @GetMapping("/{id}")
    public ResponseEntity<Workout> getWorkout(@PathVariable Long id) {
        return workoutRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing workout based on the provided ID and new workout details.
     *
     * This method handles HTTP PUT requests to update an existing workout resource
     * with the specified unique identifier. If a workout with the given ID exists,
     * its properties are updated with the values from the provided {@code Workout} object.
     * The updated workout is then saved to the repository and returned with a 200 OK response.
     *
     * If no workout exists with the specified ID, a 404 Not Found response is returned.
     *
     * @param id the unique identifier of the workout to be updated
     * @param workoutDetails the {@code Workout} object containing the updated workout details
     * @return a response containing the updated workout if successful, or a 404 Not Found response if no workout exists with the specified ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<Workout> updateWorkout(@PathVariable Long id, @RequestBody Workout workoutDetails) {
        return workoutRepository.findById(id)
                .map(workout -> {
                    workout.setWorkoutType(workoutDetails.getWorkoutType());
                    workout.setDurationMinutes(workoutDetails.getDurationMinutes());
                    workout.setCaloriesBurned(workoutDetails.getCaloriesBurned());
                    workout.setTimestamp(workoutDetails.getTimestamp());
                    workout.setAverageHeartRate(workoutDetails.getAverageHeartRate());
                    workout.setMaxHeartRate(workoutDetails.getMaxHeartRate());
                    workout.setRestingHeartRate(workoutDetails.getRestingHeartRate());
                    workout.setRecoveryTime(workoutDetails.getRecoveryTime());
                    workout.setWeatherConditions(workoutDetails.getWeatherConditions());
                    workout.setTemperature(workoutDetails.getTemperature());
                    workout.setHoursSlept(workoutDetails.getHoursSlept());
                    workout.setStressLevel(workoutDetails.getStressLevel());
                    return ResponseEntity.ok(workoutRepository.save(workout));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes an existing workout based on the provided ID.
     *
     * This method handles HTTP DELETE requests to remove a workout resource
     * with the specified unique identifier from the repository. If a workout
     * with the given ID exists, it is deleted, and a 200 OK response is returned.
     * If no workout exists with the specified ID, a 404 Not Found response is returned.
     *
     * @param id the unique identifier of the workout to be deleted
     * @return a response indicating the deletion was successful (200 OK),
     *         or a 404 Not Found response if no workout exists with the specified ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable Long id) {
        return workoutRepository.findById(id)
                .map(workout -> {
                    workoutRepository.delete(workout);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a list of workouts for a specified user within a given date and time range.
     *
     * This method handles HTTP GET requests to fetch all workouts associated with
     * the specified user ID that occurred between the provided start and end timestamps.
     * If the user is not found, a RuntimeException is thrown, resulting in an error response.
     *
     * The date-time parameters are expected in ISO 8601 format.
     *
     * @param userId the unique identifier of the user whose workouts are being queried
     * @param start the start of the date and time range for the query
     * @param end the end of the date and time range for the query
     * @return a response containing a list of workouts that match the criteria or an error response if the user is not found
     */
    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<Workout>> getWorkoutsByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(workoutRepository.findByUserAndTimestampBetween(user, start, end));
    }

    /**
     * Retrieves a list of workouts filtered by workout type.
     *
     * This method handles HTTP GET requests to fetch all workouts that match the
     * specified workout type. The workout type is provided as a path variable.
     *
     * If no workouts are found for the specified type, an empty list is returned.
     *
     * @param workoutType the type of workouts to be retrieved
     * @return a response containing a list of workouts matching the specified type,
     *         or an empty list if no workouts are found
     */
    @GetMapping("/type/{workoutType}")
    public ResponseEntity<List<Workout>> getWorkoutsByType(@PathVariable String workoutType) {
        return ResponseEntity.ok(workoutRepository.findByWorkoutType(workoutType));
    }

    /**
     * Retrieves the average calories burned by workout type for a specific user.
     *
     * This method handles HTTP GET requests to provide a mapping of workout types
     * to the average calories burned for each type, for the user identified by the
     * given user ID. If the user is not found, a RuntimeException is thrown, resulting
     * in an appropriate error response.
     *
     * The response contains the workout type as the key and the average calories burned
     * as the value.
     *
     * @param userId the unique identifier of the user for whom the average calories
     *               burned by workout type are being retrieved
     * @return a response containing a mapping of workout types to average calories burned,
     *         or an error response if the user is not found
     */
    @GetMapping("/user/{userId}/calories-by-type")
    public ResponseEntity<Map<String, Double>> getAverageCaloriesByType(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(workoutRepository.getAverageCaloriesByWorkoutType(user));
    }

    /**
     * Retrieves the total workout count for a specific user.
     *
     * This method handles HTTP GET requests to fetch the count of workouts
     * associated with the given user ID. If the user does not exist, a
     * RuntimeException is thrown, resulting in an error response.
     *
     * @param userId the unique identifier of the user whose workout count is being retrieved
     * @return a response containing the total count of workouts performed by the user,
     *         or an error response if the user is not found
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getWorkoutCount(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(workoutRepository.countByUser(user));
    }

    /**
     * Retrieves all workouts associated with a specific user.
     *
     * This method handles HTTP GET requests to fetch a list of all workouts
     * performed by the user identified by the provided user ID. If the user
     * cannot be found, a RuntimeException is thrown, resulting in an
     * appropriate error response.
     *
     * @param userId the unique identifier of the user whose workouts are to be retrieved
     * @return a response containing a list of workouts belonging to the specified user,
     *         or an error response if the user is not found
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Workout>> getUserWorkouts(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(workoutRepository.findByUser(user));
    }

    /**
     * Retrieves the average calories burned by a specific user across all their workouts.
     *
     * This method handles HTTP GET requests to calculate and return the average
     * number of calories burned by the user identified by the provided user ID.
     * If the user is not found, a RuntimeException is thrown, resulting in an
     * appropriate error response.
     *
     * @param userId the unique identifier of the user whose average calories burned is to be retrieved
     * @return a response containing the user's average calories burned as a {@code Double},
     *         or an error response if the user is not found
     */
    @GetMapping("/user/{userId}/average-calories")
    public ResponseEntity<Double> getAverageCaloriesBurned(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(workoutRepository.getAverageCaloriesBurnedByUser(user));
    }

    /**
     * Retrieves a list of recent workouts for a specific user starting from a given date and time.
     *
     * This method handles HTTP GET requests to fetch all workouts associated with the
     * user identified by the provided user ID that have occurred since the specified
     * date and time. If the user cannot be found, a RuntimeException is thrown, resulting
     * in an appropriate error response.
     *
     * The date-time parameter is expected in ISO 8601 format.
     *
     * @param userId the unique identifier of the user whose recent workouts are being retrieved
     * @param since the starting date and time from which workouts are to be fetched
     * @return a response containing a list of workouts that have occurred since the specified date and time,
     *         or an error response if the user is not found
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<Workout>> getRecentWorkouts(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(workoutRepository.findRecentWorkouts(user, since));
    }

    /**
     * Retrieves workout analytics for a specific user.
     *
     * This method handles HTTP GET requests to generate and return analytics
     * data for the user identified by the provided user ID. It utilizes
     * the {@code WorkoutAnalyticsService} to compute detailed analysis
     * of the user's workout history. If the user is not found, a
     * RuntimeException is thrown, resulting in an appropriate error response.
     *
     * The analytics data is returned as a map containing key-value pairs
     * that represent various metrics and insights related to the user's
     * workouts.
     *
     * @param userId the unique identifier of the user whose analytics are to be generated
     * @return a response containing the analytics data as a map, or
     *         an error response if the user is not found
     */
    @GetMapping("/analytics/{userId}")
    public ResponseEntity<Map<String, Object>> getAnalytics(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(analyticsService.generateWorkoutAnalytics(user));
    }

    /**
     * Retrieves machine learning data for a specified user.
     *
     * This method handles HTTP GET requests to fetch data prepared for machine learning
     * purposes for the user identified by the provided user ID. The data is returned
     * as a list of arrays of doubles, where each array represents a record of features
     * prepared for analysis or modeling.
     *
     * If the user is not found, a RuntimeException is thrown, resulting in an error response.
     *
     * @param userId the unique identifier of the user whose machine learning data is being retrieved
     * @return a response containing a list of double arrays representing the machine learning data
     *         for the user, or an error response if the user is not found
     */
    @GetMapping("/ml-data/{userId}")
    public ResponseEntity<List<double[]>> getMLData(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(analyticsService.prepareDataForML(user));
    }
}