package com.example.fitness_tracking.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.fitness_tracking.controller",
    "com.example.fitness_tracking.service",
    "com.example.fitness_tracking.repository",
"com.example.fitness_tracking.validator"
})
@EntityScan("com.example.fitness_tracking.model")
@EnableJpaRepositories("com.example.fitness_tracking.repository")
public class FitnessTrackingApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitnessTrackingApplication.class, args);
    }
}