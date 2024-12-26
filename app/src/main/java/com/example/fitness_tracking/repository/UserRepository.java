package com.example.fitness_tracking.repository;

import com.example.fitness_tracking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
/**
 * UserRepository interface provides methods for performing CRUD operations and custom queries
 * on User entities. It extends JpaRepository to inherit basic repository functionality.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findByFitnessLevel(User.FitnessLevel fitnessLevel);
}