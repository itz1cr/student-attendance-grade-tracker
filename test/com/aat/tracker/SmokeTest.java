package com.aat.tracker;

import com.aat.tracker.dao.AttendanceDAO;
import com.aat.tracker.dao.CourseDAO;
import com.aat.tracker.dao.EnrollmentDAO;
import com.aat.tracker.dao.StudentDAO;
import com.aat.tracker.db.DatabaseManager;
import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.AssessmentType;
import com.aat.tracker.model.AttendanceEntry;
import com.aat.tracker.model.AttendanceStat;
import com.aat.tracker.model.AttendanceStatus;
import com.aat.tracker.model.Course;
import com.aat.tracker.model.Grade;
import com.aat.tracker.model.GradeLetter;
import com.aat.tracker.model.Student;
import com.aat.tracker.service.AttendanceService;
import com.aat.tracker.service.GradeService;
import com.aat.tracker.util.Statistics;
import com.aat.tracker.util.Validator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Console smoke test for unit/module testing (Week 9: Integration, Testing
 * &amp; Debugging). Runs against an in-memory SQLite database and prints a
 * PASS/FAIL summary; the process exit code is 0 only when everything passes.
 */
public final class SmokeTest {

    private static int passed = 0;
    private static int failed = 0;

    private SmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== SmokeTest: Student Attendance & Grade Tracker ===");
        // Shared in-memory database: visible to every connection in this JVM.
        System.setProperty("tracker.db.url",
                "jdbc:sqlite:file:smoketest?mode=memory&cache=shared");
        DatabaseManager.getInstance().initDatabase();

        testModelValidation();
        testStudentCrud();
        testCourseCrudAndEnrollment();
        testAttendanceFlow();
        testGradeFlowAndGpa();
        testStatisticsAndValidator();
        testCsvExport();

        System.out.println();
        System.out.printf("RESULT: %d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.out.println("SMOKE TEST FAILED - fix the failing checks above.");
            System.exit(1);
        }
        System.out.println("SMOKE TEST PASSED - all modules work together correctly.");
    }

    // ------------------------------------------------------------------
    private static void testModelValidation() {
        section("Model validation (user-defined ValidationException)");
        check("blank name rejected", throwsException(() ->
                new Student("CSE1", "  ", "CSE", 1), ValidationException.class));
        check("invalid email rejected", throwsException(() ->
                new Student("CSE1", "Nikhil", "bad-email", "9876500001",
                        "CSE", 1, "A"), ValidationException.class));
        check("invalid semester rejected", throwsException(() ->
                new Student("CSE1", "Nikhil", "CSE", 9), ValidationException.class));
        check("negative credits rejected", throwsException(() ->
                new Course("CS101", "Intro", -2, "Dr. X", 1), ValidationException.class));
        check("valid student accepted", passes(() ->
                new Student("CSE101", "Nikhil", "nikhil@example.com",
                        "9876500001", "CSE", 1, "A")));
    }

    private static void testStudentCrud() throws Exception {
        section("StudentDAO CRUD (PreparedStatement)");
        StudentDAO dao = new StudentDAO();

        Student student = new Student("CSE21001", "Aarav Sharma",
                "aarav@example.com", "9876500011", "CSE", 5, "A");
        dao.save(student);
        check("insert assigns a positive id", student.getStudentId() > 0);

        var found = dao.findById(student.getStudentId());
        check("findById returns the saved student",
                found.isPresent() && found.get().getRollNumber().equals("CSE21001"));

        student.setName("Aarav S.");
        dao.save(student);
        check("update persists new values",
                dao.findById(student.getStudentId()).orElseThrow().getName().equals("Aarav S."));

        check("search matches by name", dao.search("aarav").size() == 1);

        Student second = new Student("CSE21002", "Diya Patel", "CSE", 5);
        dao.save(second);
        check("findAll returns all rows", dao.findAll().size() == 2);

        check("duplicate roll number rejected", throwsException(
                () -> dao.save(new Student("CSE21001", "Another", "CSE", 5)),
                DatabaseException.class));

        check("deleteById removes the row", dao.deleteById(second.getStudentId())
                && dao.findAll().size() == 1);
    }

    private static void testCourseCrudAndEnrollment() throws Exception {
        section("CourseDAO + EnrollmentDAO (tables and relationships)");
        CourseDAO courseDao = new CourseDAO();
        StudentDAO studentDao = new StudentDAO();
        EnrollmentDAO enrollmentDao = new EnrollmentDAO();

        Course course = new Course("CS301", "Data Structures", 4, "Dr. K. Menon", 5);
        courseDao.save(course);
        check("course insert assigns an id", course.getCourseId() > 0);

        Student student = studentDao.findAll().get(0);

        enrollmentDao.enroll(student.getStudentId(), course.getCourseId());
        check("isEnrolled after enroll", enrollmentDao.isEnrolled(
                student.getStudentId(), course.getCourseId()));
        check("findByCourse returns the enrolment",
                enrollmentDao.findByCourse(course.getCourseId()).size() == 1);
        check("findByStudent returns the enrolment",
                enrollmentDao.findByStudent(student.getStudentId()).size() == 1);

        check("unenroll removes the link", enrollmentDao.unenroll(
                student.getStudentId(), course.getCourseId())
                && !enrollmentDao.isEnrolled(student.getStudentId(), course.getCourseId()));
    }

    private static void testAttendanceFlow() throws Exception {
        section("Attendance module (bulk marking + statistics)");
        CourseDAO courseDao = new CourseDAO();
        StudentDAO studentDao = new StudentDAO();
        EnrollmentDAO enrollmentDao = new EnrollmentDAO();
        AttendanceDAO attendanceDao = new AttendanceDAO();
        AttendanceService service = new AttendanceService();

        Course course = courseDao.findAll().get(0);
        List<Student> students = studentDao.findAll();
        for (Student student : students) {
            enrollmentDao.enroll(student.getStudentId(), course.getCourseId());
        }
        Student first = students.get(0);

        LocalDate day1 = LocalDate.of(2026, 8, 3);
        LocalDate day2 = LocalDate.of(2026, 8, 5);

        List<AttendanceEntry> day1Sheet = new ArrayList<>();
        for (Student student : students) {
            day1Sheet.add(new AttendanceEntry(student.getStudentId(),
                    AttendanceStatus.PRESENT, ""));
        }
        service.markClassAttendance(course.getCourseId(), day1, day1Sheet);
        check("bulk marking saved for every student",
                attendanceDao.getStatusMapForDate(course.getCourseId(), day1).size()
                        == students.size());

        // Day 2: only the first student attends.
        List<AttendanceEntry> day2Sheet = List.of(
                new AttendanceEntry(first.getStudentId(), AttendanceStatus.PRESENT, ""));
        service.markClassAttendance(course.getCourseId(), day2, day2Sheet);

        // Upsert: re-save day 2 for the first student as ABSENT.
        service.markClassAttendance(course.getCourseId(), day2, List.of(
                new AttendanceEntry(first.getStudentId(), AttendanceStatus.ABSENT, "Sick")));
        check("re-marking a day updates the record (upsert)",
                attendanceDao.getStatusMapForDate(course.getCourseId(), day2)
                        .get(first.getStudentId()) == AttendanceStatus.ABSENT);

        List<AttendanceStat> stats = service.getCourseStatistics(course.getCourseId());
        AttendanceStat firstStat = stats.stream()
                .filter(stat -> stat.studentId() == first.getStudentId())
                .findFirst().orElseThrow();
        check("statistics: 1 present + 1 absent of 2 classes",
                firstStat.present() == 1 && firstStat.absent() == 1
                        && firstStat.totalClasses() == 2);
        check("percentage computed as 50.0",
                Math.abs(firstStat.attendancePercentage() - 50.0) < 0.001);

        check("marking an unknown student fails with ValidationException",
                throwsException(() -> service.markClassAttendance(course.getCourseId(), day1,
                        List.of(new AttendanceEntry(99999, AttendanceStatus.PRESENT, ""))),
                        ValidationException.class));
    }

    private static void testGradeFlowAndGpa() throws Exception {
        section("Grade module (weighted percentage, letters, GPA)");
        CourseDAO courseDao = new CourseDAO();
        StudentDAO studentDao = new StudentDAO();
        EnrollmentDAO enrollmentDao = new EnrollmentDAO();
        GradeService gradeService = new GradeService();

        Student student = studentDao.findAll().get(0);
        Course course = courseDao.findAll().get(0);
        if (!enrollmentDao.isEnrolled(student.getStudentId(), course.getCourseId())) {
            enrollmentDao.enroll(student.getStudentId(), course.getCourseId());
        }

        gradeService.saveGrade(new Grade(student.getStudentId(), course.getCourseId(),
                AssessmentType.QUIZ, 15, 20, 10, LocalDate.now()));
        gradeService.saveGrade(new Grade(student.getStudentId(), course.getCourseId(),
                AssessmentType.FINAL, 70, 100, 50, LocalDate.now()));

        double expected = (75.0 * 10.0 + 70.0 * 50.0) / 60.0; // 70.833...
        double actual = GradeService.weightedPercentage(gradeService.getGrades(
                student.getStudentId(), course.getCourseId()));
        check("weighted percentage across assessments",
                Math.abs(actual - expected) < 0.001);

        check("letter grade boundary 90 -> A+", GradeLetter.fromPercentage(90.0) == GradeLetter.A_PLUS);
        check("letter grade boundary 89.9 -> A", GradeLetter.fromPercentage(89.9) == GradeLetter.A);
        check("letter grade boundary 75 -> B+", GradeLetter.fromPercentage(75.0) == GradeLetter.B_PLUS);
        check("letter grade boundary 39.9 -> F", GradeLetter.fromPercentage(39.9) == GradeLetter.F);

        check("marks above maximum rejected", throwsException(() ->
                        gradeService.saveGrade(new Grade(student.getStudentId(),
                                course.getCourseId(), AssessmentType.QUIZ, 25)),
                ValidationException.class));

        // GPA: 70.83% in this 4-credit course -> letter B+ -> 8.0 points.
        double gpa = gradeService.getGpa(student.getStudentId());
        check("GPA equals the letter's grade points for one course",
                Math.abs(gpa - 8.0) < 0.001);
    }

    private static void testStatisticsAndValidator() throws Exception {
        section("Statistics (generics) + Validator (static helpers)");
        check("mean of [2,4,6] is 4", Math.abs(Statistics.mean(List.of(2, 4, 6)) - 4.0) < 0.001);
        check("median of [1,3,7] is 3", Math.abs(Statistics.median(List.of(7, 1, 3)) - 3.0) < 0.001);
        check("email format check", Validator.isValidEmail("a.b@example.com")
                && !Validator.isValidEmail("not-an-email"));
        check("invalid date rejected", throwsException(
                () -> Validator.parseDate("2026-13-99", "date"), ValidationException.class));
        check("valid date parsed", Validator.parseDate("2026-08-03", "date")
                .equals(LocalDate.of(2026, 8, 3)));
    }

    private static void testCsvExport() throws Exception {
        section("CSV export (generic CsvExporter + File I/O)");
        Path temp = Files.createTempFile("tracker-export-", ".csv");
        try {
            var exporter = new com.aat.tracker.util.CsvExporter<Student>(
                    "Roll Number,Name,Email,Phone,Department,Semester,Section");
            List<Student> students = new StudentDAO().findAll();
            Path written = exporter.export(temp, students);
            List<String> lines = Files.readAllLines(written);
            check("header + one line per student", lines.size() == students.size() + 1);
            check("header written first", lines.get(0).startsWith("Roll Number"));
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    // ------------------------------------------------------------------
    private static void section(String title) {
        System.out.println();
        System.out.println("-- " + title);
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name);
        }
    }

    /** True when the code block throws an exception assignable to {@code type}. */
    private static boolean throwsException(ThrowingRunnable code,
                                           Class<? extends Throwable> type) {
        try {
            code.run();
            return false;
        } catch (Throwable thrown) {
            return type.isAssignableFrom(thrown.getClass());
        }
    }

    /** True when the code block runs without throwing anything. */
    private static boolean passes(ThrowingRunnable code) {
        try {
            code.run();
            return true;
        } catch (Throwable thrown) {
            return false;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
