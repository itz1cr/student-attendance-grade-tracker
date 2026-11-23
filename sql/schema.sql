-- ============================================================================
--  Student Attendance & Grade Tracker  -  Database Schema (SQLite)
--  Java AAT Project | Week 7: Database & JDBC Implementation
-- ----------------------------------------------------------------------------
--  Tables        : students, courses, enrollments, attendance, grades
--  Relationships : enrollments  (M:N between students and courses)
--                  attendance   (M:N + date; one record per student per class)
--                  grades       (one record per student/course/assessment)
--  Note: the exact same DDL is embedded in DatabaseManager.java and executed
--        automatically at startup (CREATE TABLE IF NOT EXISTS).
-- ============================================================================

PRAGMA foreign_keys = ON;

-- Students -------------------------------------------------------------------
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

-- Courses --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courses (
    course_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    course_code TEXT    NOT NULL UNIQUE,
    title       TEXT    NOT NULL,
    credits     INTEGER NOT NULL DEFAULT 3 CHECK (credits BETWEEN 1 AND 6),
    instructor  TEXT    NOT NULL DEFAULT '',
    semester    INTEGER NOT NULL CHECK (semester BETWEEN 1 AND 8)
);

-- Enrollments (M:N between students and courses) -----------------------------
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id    INTEGER NOT NULL,
    course_id     INTEGER NOT NULL,
    enrolled_on   TEXT    NOT NULL DEFAULT (date('now')),
    UNIQUE (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses  (course_id)  ON DELETE CASCADE
);

-- Attendance (one row per student per class date) ----------------------------
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

-- Grades (one row per student/course/assessment) -----------------------------
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

-- Performance indexes --------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_attendance_course_date ON attendance (course_id, class_date);
CREATE INDEX IF NOT EXISTS idx_grades_student        ON grades     (student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course    ON enrollments (course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_student   ON enrollments (student_id);
