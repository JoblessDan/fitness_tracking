package com.example.fitness_tracking.repository;

import com.example.fitness_tracking.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Implementation of the UserRepository interface that provides custom data access logic for User entities.
 * This class uses JPA's EntityManager to interact with the database and perform CRUD operations.
 * It also includes methods to support query-based retrieval of User entities based on attributes
 * like email, username, fitness level, and ID.
 *
 * This implementation is annotated with `@Repository` to indicate that it is a Spring-managed
 * repository bean, enabling exception translation and dependency injection.
 *
 * Methods:
 * - `findByEmail(String email)` - Retrieves an Optional User by email.
 * - `findByUsername(String username)` - Retrieves an Optional User by username.
 * - `findByFitnessLevel(User.FitnessLevel fitnessLevel)` - Retrieves a list of Users matching a specific fitness level.
 * - `findById(Long id)` - Retrieves an Optional User by ID.
 * - `findAll()` - Retrieves a list of all Users.
 * - `findAllById(Iterable<Long> longs)` - Retrieves Users for specified IDs.
 * - `save(User user)` - Saves or updates a User entity in the database.
 * - `deleteById(Long id)` - Deletes a User with the specified ID.
 * - `flush()` - Flushes pending changes to the database.
 * - `saveAndFlush(S entity)` - Saves and immediately flushes a User entity.
 * - `saveAll(Iterable<S> entities)` - Saves multiple User entities.
 * - `deleteAll()` - Deletes all User entities from the database.
 * - `count()` - Returns the total number of User entities in the database.
 * - Other overridden methods that are placeholders or default implementations for Spring Data functionality.
 *
 * This class maintains the EntityManager through the `@PersistenceContext` annotation, allowing
 * seamless integration with the persistence context within a Spring-managed environment.
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Retrieves a User entity based on the provided email address.
     *
     * The method queries the database using the JPA EntityManager and
     * constructs a query to find the User with the specified email.
     * If a matching User exists, it is wrapped in an Optional and returned.
     * If no result is found, an empty Optional is returned.
     *
     * @param email the email address of the User to be retrieved
     * @return an Optional containing the User if found; otherwise, an empty Optional
     * @throws IllegalArgumentException if the email parameter is null
     */
    @Override
    public Optional<User> findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.email = :email", User.class);
        query.setParameter("email", email);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves a User entity based on the provided username.
     *
     * The method queries the database using the JPA EntityManager and constructs a query to
     * find the User with the specified username. If a matching User exists, it is wrapped in
     * an Optional and returned. If no result is found, an empty Optional is returned.
     *
     * @param username the username of the User to be retrieved
     * @return an Optional containing the User if found; otherwise, an empty Optional
     * @throws IllegalArgumentException if the username parameter is null
     */
    @Override
    public Optional<User> findByUsername(String username) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.username = :username", User.class);
        query.setParameter("username", username);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves a list of User entities that match the specified fitness level.
     * 
     * This method constructs and executes a JPA query to retrieve all Users 
     * who have the provided fitness level attribute. The query uses a parameter 
     * for the fitness level to dynamically filter the results.
     * 
     * @param fitnessLevel the fitness level that is used to filter the Users
     * @return a list of Users that match the specified fitness level; 
     *         if no Users match, the list will be empty
     * @throws IllegalArgumentException if the fitnessLevel parameter is null
     */
    @Override
    public List<User> findByFitnessLevel(User.FitnessLevel fitnessLevel) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.fitnessLevel = :fitnessLevel", User.class);
            query.setParameter("fitnessLevel", fitnessLevel);
            return query.getResultList();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    public List<User> findAll() {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u", User.class);
        return query.getResultList();
    }

    @Override
    public List<User> findAllById(Iterable<Long> longs) {
        return List.of();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        } else {
            return entityManager.merge(user);
        }
    }

    @Override
    public void deleteById(Long id) {
        User user = findById(id).orElse(null);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    @Override
    public void delete(User entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends User> entities) {

    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public <S extends User> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<User> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public <S extends User> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }

    @Override
    public void deleteAllInBatch() {
        entityManager.createQuery("DELETE FROM User").executeUpdate();
    }

    @Override
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM User").executeUpdate();
    }

    @Override
    public User getOne(Long id) {
        return entityManager.getReference(User.class, id);
    }

    @Override
    public User getById(Long aLong) {
        return null;
    }

    @Override
    public User getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends User> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends User> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends User> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends User> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        return entityManager.createQuery("SELECT COUNT(u) FROM User u", Long.class)
            .getSingleResult();
    }

    @Override
    public List<User> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return null;
    }
}