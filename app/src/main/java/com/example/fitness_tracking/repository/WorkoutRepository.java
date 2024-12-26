package com.example.fitness_tracking.repository;

import com.example.fitness_tracking.model.User;
import com.example.fitness_tracking.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Repository interface for managing `Workout` entities.
 * Provides methods for performing CRUD operations
 * and custom queries for analytics and data retrieval specific to workouts.
 */
@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);
    List<Workout> findByWorkoutType(String workoutType);

    @Query("SELECT w.workoutType, AVG(w.caloriesBurned) FROM Workout w WHERE w.user = ?1 GROUP BY w.workoutType")
    Map<String, Double> getAverageCaloriesByWorkoutType(User user);

    Long countByUser(User user);
    List<Workout> findByUser(User user);

    @Query("SELECT AVG(w.caloriesBurned) FROM Workout w WHERE w.user = ?1")
    Double getAverageCaloriesBurnedByUser(User user);

    List<Workout> findByUserOrderByTimestampDesc(User user);

    @Query("SELECT w FROM Workout w WHERE w.user = ?1 AND w.timestamp >= ?2")
    List<Workout> findRecentWorkouts(User user, LocalDateTime since);
}