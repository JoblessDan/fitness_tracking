package com.example.fitness_tracking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "workouts")
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime timestamp;
    private String workoutType;
    private Integer durationMinutes;
    private Double caloriesBurned;
    private Integer averageHeartRate;

    // Additional metrics (ML)
    private Double maxHeartRate;
    private Double restingHeartRate;
    private Double recoveryTime;
    private String weatherConditions;
    private Double temperature;
    private Integer hoursSlept;
    private Integer stressLevel;

}