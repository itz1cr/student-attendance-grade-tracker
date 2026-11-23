package com.aat.tracker.db;

import com.aat.tracker.exception.DatabaseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Central JDBC connection manager (Week 7: JDBC connection).
 *
 * <ul>
 *   <li>Singleton design pattern: one manager for the whole application
 *       (Week 4: static members, private constructor).</li>
 *   <li>Creates the SQLite database file and the full schema automatically
 *       (Week 7: database creation).</li>
 *   <li>Enables foreign-key enforcement on every connection.</li>
 *   <li>The JDBC URL can be overridden with {@code -Dtracker.db.url=...}
 *       (e.g. {@code jdbc:sqlite:file:testdb?mode=memory&cache=shared}).</li>
 * </ul>
 */
public final class DatabaseManager {

    /** Default SQLite database file, created relative to the project root. */
    public static final String DEFAULT_DB_PATH = "data/tracker.db";

    private static volatile DatabaseManager instance;

    private final String jdbcUrl;

    /** Keeps an in-memory database alive between DAO connections. */
    private Connection keepAliveConnection;

    private DatabaseManager() {
        String url = System.getProperty("tracker.db.url");
        if (url == null || url.isBlank()) {
            Path dbPath = Path.of(DEFAULT_DB_PATH).toAbsolutePath();
            try {
                Files.createDirectories(dbPath.getParent());
            } catch (IOException e) {
                throw new DatabaseException(
                        "Could not create data directory for the database.", e);
            }
            jdbcUrl = "jdbc:sqlite:" + dbPath;
        } else {
            jdbcUrl = url;
        }
    }

    /** Double-checked locking singleton accessor. */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /** Opens a connection and turns on foreign-key enforcement. */
    public Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Could not open a database connection to " + jdbcUrl, e);
        }
    }

    /** Creates all tables (idempotent: CREATE TABLE IF NOT EXISTS). */
    public void initDatabase() {
        // An SQLite in-memory database is destroyed when its LAST connection
        // closes, so the keep-alive connection must be opened BEFORE the
        // schema connection is closed (used by the smoke tests).
        if (jdbcUrl.contains("mode=memory") && keepAliveConnection == null) {
            keepAliveConnection = getConnection();
        }
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            for (String ddl : SCHEMA_SQL.split(";")) {
                String sql = ddl.trim();
                if (!sql.isEmpty()) {
                    statement.execute(sql);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not initialise the database schema.", e);
        }
    }

    /** Releases the keep-alive connection, if any. */
    public void close() {
        if (keepAliveConnection != null) {
            try {
                keepAliveConnection.close();
            } catch (SQLException ignored) {
                // nothing sensible left to do during shutdown
            }
            keepAliveConnection = null;
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * The exact same DDL is kept in {@code sql/schema.sql} for reference.
     * Java 15+ text block.
     */
    private static final String SCHEMA_SQL = """
            CREATE TABLE IF NOT EXISTS students (
                student_id  INTEGER PRIMARY KEY AUTOINCREMENT,
                roll_number TEXT    NOT NULL UNIQUE,
                name        TEXT    NOT NULL,
                email       TEXT    NOT NULL DEFAULT '',
                phone       TEXT    NOT NULL DEFAULT '',
                department  TEXT    NOT NULL,
                semester    INTEGER NOT NULL CHECK (semester BETWEEN 1 AND 8),
                section     TEXT    NOT NULL DEFAULT ''
            );

            CREATE TABLE IF NOT EXISTS courses (
                course_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                course_code TEXT    NOT NULL UNIQUE,
                title       TEXT    NOT NULL,
                credits     INTEGER NOT NULL DEFAULT 3 CHECK (credits BETWEEN 1 AND 6),
                instructor  TEXT    NOT NULL DEFAULT '',
                semester    INTEGER NOT NULL CHECK (semester BETWEEN 1 AND 8)
            );

            CREATE TABLE IF NOT EXISTS enrollments (
                enrollment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id    INTEGER NOT NULL,
                course_id     INTEGER NOT NULL,
                enrolled_on   TEXT    NOT NULL DEFAULT (date('now')),
                UNIQUE (student_id, course_id),
                FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
                FOREIGN KEY (course_id)  REFERENCES courses  (course_id)  ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS attendance (
                attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id    INTEGER NOT NULL,
                course_id     INTEGER NOT NULL,
                class_date    TEXT    NOT NULL,
                status        TEXT    NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'LATE')),
                remark        TEXT    NOT NULL DEFAULT '',
                UNIQUE (student_id, course_id, class_date),
                FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
                FOREIGN KEY (course_id)  REFERENCES courses  (course_id)  ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS grades (
                grade_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id     INTEGER NOT NULL,
                course_id      INTEGER NOT NULL,
                assessment     TEXT    NOT NULL CHECK (assessment IN ('QUIZ','ASSIGNMENT','MIDTERM','FINAL','PROJECT')),
                marks_obtained REAL    NOT NULL CHECK (marks_obtained >= 0),
                max_marks      REAL    NOT NULL CHECK (max_marks > 0),
                weight_percent REAL    NOT NULL CHECK (weight_percent BETWEEN 0 AND 100),
                recorded_on    TEXT    NOT NULL DEFAULT (date('now')),
                UNIQUE (student_id, course_id, assessment),
                FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
                FOREIGN KEY (course_id)  REFERENCES courses  (course_id)  ON DELETE CASCADE
            );

            CREATE INDEX IF NOT EXISTS idx_attendance_course_date ON attendance (course_id, class_date);
            CREATE INDEX IF NOT EXISTS idx_grades_student        ON grades     (student_id);
            CREATE INDEX IF NOT EXISTS idx_enrollments_course    ON enrollments (course_id);
            CREATE INDEX IF NOT EXISTS idx_enrollments_student   ON enrollments (student_id);
            """;
}
