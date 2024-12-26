package com.example.fitness_tracking.repository;

import com.example.fitness_tracking.model.User;
import com.example.fitness_tracking.model.Workout;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Repository
public class WorkoutRepositoryImpl implements WorkoutRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Workout> findByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end) {
        TypedQuery<Workout> query = entityManager.createQuery(
            "SELECT w FROM Workout w WHERE w.user = :user AND w.timestamp BETWEEN :start AND :end",
            Workout.class);
        query.setParameter("user", user);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    @Override
    public List<Workout> findByWorkoutType(String workoutType) {
        TypedQuery<Workout> query = entityManager.createQuery(
            "SELECT w FROM Workout w WHERE w.workoutType = :workoutType",
            Workout.class);
        query.setParameter("workoutType", workoutType);
        return query.getResultList();
    }

    @Override
    public Map<String, Double> getAverageCaloriesByWorkoutType(User user) {
        List<Object[]> results = entityManager.createQuery(
            "SELECT w.workoutType, AVG(w.caloriesBurned) FROM Workout w WHERE w.user = :user GROUP BY w.workoutType",
            Object[].class)
            .setParameter("user", user)
            .getResultList();

        Map<String, Double> averages = new HashMap<>();
        for (Object[] result : results) {
            averages.put((String) result[0], (Double) result[1]);
        }
        return averages;
    }

    @Override
    public Long countByUser(User user) {
        return entityManager.createQuery(
            "SELECT COUNT(w) FROM Workout w WHERE w.user = :user", Long.class)
            .setParameter("user", user)
            .getSingleResult();
    }

    @Override
    public List<Workout> findByUser(User user) {
        return entityManager.createQuery(
            "SELECT w FROM Workout w WHERE w.user = :user", Workout.class)
            .setParameter("user", user)
            .getResultList();
    }

    @Override
    public Double getAverageCaloriesBurnedByUser(User user) {
        return entityManager.createQuery(
            "SELECT AVG(w.caloriesBurned) FROM Workout w WHERE w.user = :user", Double.class)
            .setParameter("user", user)
            .getSingleResult();
    }

    @Override
    public List<Workout> findByUserOrderByTimestampDesc(User user) {
        return entityManager.createQuery(
            "SELECT w FROM Workout w WHERE w.user = :user ORDER BY w.timestamp DESC", Workout.class)
            .setParameter("user", user)
            .getResultList();
    }

    @Override
    public List<Workout> findRecentWorkouts(User user, LocalDateTime since) {
        return entityManager.createQuery(
            "SELECT w FROM Workout w WHERE w.user = :user AND w.timestamp >= :since", Workout.class)
            .setParameter("user", user)
            .setParameter("since", since)
            .getResultList();
    }

    // Standard JPA repository methods
    @Override
    public Optional<Workout> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Workout.class, id));
    }

    @Override
    public List<Workout> findAll() {
        return entityManager.createQuery("SELECT w FROM Workout w", Workout.class)
            .getResultList();
    }

    @Override
    public Workout save(Workout workout) {
        if (workout.getId() == null) {
            entityManager.persist(workout);
            return workout;
        } else {
            return entityManager.merge(workout);
        }
    }

    @Override
    public void deleteById(Long id) {
        Workout workout = findById(id).orElse(null);
        if (workout != null) {
            entityManager.remove(workout);
        }
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public <S extends Workout> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }

    @Override
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM Workout").executeUpdate();
    }

    // Required method implementations with default behavior
    @Override
    public List<Workout> findAllById(Iterable<Long> longs) {
        return new ArrayList<>();
    }

    @Override
    public void delete(Workout entity) {
        entityManager.remove(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
    }

    @Override
    public void deleteAll(Iterable<? extends Workout> entities) {
    }

    @Override
    public <S extends Workout> S saveAndFlush(S entity) {
        S result = (S) save(entity);
        flush();
        return result;
    }

    @Override
    public <S extends Workout> List<S> saveAllAndFlush(Iterable<S> entities) {
        List<S> result = saveAll(entities);
        flush();
        return result;
    }

    @Override
    public void deleteAllInBatch(Iterable<Workout> entities) {
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public Workout getOne(Long id) {
        return entityManager.getReference(Workout.class, id);
    }

    @Override
    public Workout getById(Long id) {
        return entityManager.find(Workout.class, id);
    }

    @Override
    public Workout getReferenceById(Long id) {
        return entityManager.getReference(Workout.class, id);
    }

    @Override
    public <S extends Workout> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Workout> List<S> findAll(Example<S> example) {
        return new ArrayList<>();
    }

    @Override
    public <S extends Workout> List<S> findAll(Example<S> example, Sort sort) {
        return new ArrayList<>();
    }

    @Override
    public <S extends Workout> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Workout> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Workout> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Workout, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        return entityManager.createQuery("SELECT COUNT(w) FROM Workout w", Long.class)
            .getSingleResult();
    }

    @Override
    public List<Workout> findAll(Sort sort) {
        return new ArrayList<>();
    }

    @Override
    public Page<Workout> findAll(Pageable pageable) {
        return null;
    }
}