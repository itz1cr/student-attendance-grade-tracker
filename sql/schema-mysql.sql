-- ============================================================================
--  Student Attendance & Grade Tracker - ALTERNATIVE schema for MySQL 8.x
-- ----------------------------------------------------------------------------
--  The application ships with an embedded SQLite database (zero setup).
--  If your college requires MySQL for the viva, this DDL creates the same
--  logical design on a MySQL server. The DAO layer uses standard JDBC +
--  PreparedStatement, so swapping the driver is straightforward; the only
--  SQLite-specific statement is the ON CONFLICT upsert in AttendanceDAO and
--  GradeDAO, which maps to MySQL's INSERT ... ON DUPLICATE KEY UPDATE.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS attendance_tracker;
USE attendance_tracker;

CREATE TABLE IF NOT EXISTS students (
    student_id  INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(20)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL DEFAULT '',
    phone       VARCHAR(20)  NOT NULL DEFAULT '',
    department  VARCHAR(60)  NOT NULL,
    semester    INT          NOT NULL CHECK (semester BETWEEN 1 AND 8),
    section     VARCHAR(5)   NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS courses (
    course_id   INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(12)  NOT NULL UNIQUE,
    title       VARCHAR(100) NOT NULL,
    credits     INT          NOT NULL DEFAULT 3,
    instructor  VARCHAR(100) NOT NULL DEFAULT '',
    semester    INT          NOT NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT  NOT NULL,
    course_id     INT  NOT NULL,
    enrolled_on   DATE NOT NULL DEFAULT (CURRENT_DATE),
    UNIQUE KEY uq_enrollment (student_id, course_id),
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_enroll_course FOREIGN KEY (course_id)
        REFERENCES courses (course_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT  NOT NULL,
    course_id     INT  NOT NULL,
    class_date    DATE NOT NULL,
    status        ENUM('PRESENT','ABSENT','LATE') NOT NULL,
    remark        VARCHAR(200) NOT NULL DEFAULT '',
    UNIQUE KEY uq_attendance (student_id, course_id, class_date),
    CONSTRAINT fk_att_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_att_course FOREIGN KEY (course_id)
        REFERENCES courses (course_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS grades (
    grade_id       INT AUTO_INCREMENT PRIMARY KEY,
    student_id     INT  NOT NULL,
    course_id      INT  NOT NULL,
    assessment     ENUM('QUIZ','ASSIGNMENT','MIDTERM','FINAL','PROJECT') NOT NULL,
    marks_obtained DECIMAL(6,2) NOT NULL,
    max_marks      DECIMAL(6,2) NOT NULL,
    weight_percent DECIMAL(5,2) NOT NULL,
    recorded_on    DATE NOT NULL DEFAULT (CURRENT_DATE),
    UNIQUE KEY uq_grade (student_id, course_id, assessment),
    CONSTRAINT fk_grade_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_grade_course FOREIGN KEY (course_id)
        REFERENCES courses (course_id) ON DELETE CASCADE
);

CREATE INDEX idx_attendance_course_date ON attendance (course_id, class_date);
CREATE INDEX idx_grades_student        ON grades (student_id);
CREATE INDEX idx_enrollments_course    ON enrollments (course_id);
CREATE INDEX idx_enrollments_student   ON enrollments (student_id);
