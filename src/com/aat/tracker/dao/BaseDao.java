package com.aat.tracker.dao;

import com.aat.tracker.db.DatabaseManager;
import com.aat.tracker.exception.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class of every DAO (Week 5: abstract classes).
 *
 * <p>Encapsulates the repetitive JDBC plumbing - opening connections,
 * try-with-resources, exception wrapping - so that concrete DAOs only
 * provide the SQL and the row mapping (Week 7: JDBC, PreparedStatement;
 * Week 8: built-in exception handling).</p>
 */
public abstract class BaseDao {

    protected final DatabaseManager db;

    protected BaseDao() {
        this.db = DatabaseManager.getInstance();
    }

    /** Runs an INSERT and returns the generated primary key. */
    protected int executeInsert(String sql, PreparedStatementBinder binder) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.bind(statement);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DatabaseException("Insert did not return a generated key.");
        } catch (SQLException e) {
            throw new DatabaseException("Insert failed: " + e.getMessage(), e);
        }
    }

    /** Runs an UPDATE/DELETE and returns the number of affected rows. */
    protected int executeUpdate(String sql, PreparedStatementBinder binder) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Update failed: " + e.getMessage(), e);
        }
    }

    /** Runs a SELECT and maps every row with the given mapper. */
    protected <T> List<T> query(String sql, PreparedStatementBinder binder,
                                ResultSetMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapper.map(resultSet));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Query failed: " + e.getMessage(), e);
        }
    }

    /** Overload for parameter-less SELECT statements. */
    protected <T> List<T> query(String sql, ResultSetMapper<T> mapper) {
        return query(sql, statement -> { }, mapper);
    }

    /** Runs a scalar COUNT-style query. */
    protected int queryInt(String sql) {
        try (Connection connection = db.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Query failed: " + e.getMessage(), e);
        }
    }

    /** Functional interface for binding parameters (Week 8: lambdas). */
    @FunctionalInterface
    protected interface PreparedStatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    /** Functional interface for mapping one ResultSet row to an entity. */
    @FunctionalInterface
    protected interface ResultSetMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }
}
