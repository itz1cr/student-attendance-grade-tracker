package com.aat.tracker.dao;

import com.aat.tracker.exception.DatabaseException;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD contract shared by every entity DAO
 * (Week 5: interfaces; Week 8: generics).
 *
 * @param <T>  the entity type
 * @param <ID> the type of the primary key
 */
public interface CrudRepository<T, ID> {

    /** Inserts a new entity or updates an existing one; returns the entity. */
    T save(T entity) throws DatabaseException;

    /** Finds an entity by primary key. */
    Optional<T> findById(ID id) throws DatabaseException;

    /** Returns every entity, ordered deterministically. */
    List<T> findAll() throws DatabaseException;

    /** Deletes an entity; true when a row was actually removed. */
    boolean deleteById(ID id) throws DatabaseException;
}
