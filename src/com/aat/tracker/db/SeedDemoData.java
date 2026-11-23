package com.aat.tracker.db;

import com.aat.tracker.dao.AttendanceDAO;
import com.aat.tracker.dao.CourseDAO;
import com.aat.tracker.dao.EnrollmentDAO;
import com.aat.tracker.dao.GradeDAO;
import com.aat.tracker.dao.StudentDAO;
import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.AssessmentType;
import com.aat.tracker.model.AttendanceStatus;
import com.aat.tracker.model.Course;
import com.aat.tracker.model.Grade;
import com.aat.tracker.model.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeds the database with a realistic semester dataset the first time the
 * application runs: 12 students, 4 courses, 36 enrolments, ~1000 attendance
 * records and 180 grades. Uses a fixed {@link Random} seed so the demo data
 * is always reproducible (Week 8: java.util collections).
 */
public final class SeedDemoData {

    private static final long FIXED_RANDOM_SEED = 42L;

    private SeedDemoData() {
    }

    public static void seedIfEmpty() {
        StudentDAO studentDao = new StudentDAO();
        if (!studentDao.findAll().isEmpty()) {
            return; // data already present - never overwrite the user's work
        }
        System.out.println("[SeedDemoData] Empty database detected - creating demo data...");

        Random random = new Random(FIXED_RANDOM_SEED);

        List<Student> students = createStudents(studentDao);
        List<Course> courses = createCourses(new CourseDAO());

        EnrollmentDAO enrollmentDao = new EnrollmentDAO();
        int enrollmentCount = 0;
        for (Student student : students) {
            for (Course course : courses) {
                boolean takesCourse = course.getCourseCode().equals("CS302")
                        || course.getCourseCode().equals("CS303")
                        || (student.getSection().equals("A")
                            && course.getCourseCode().equals("CS301"))
                        || (student.getSection().equals("B")
                            && course.getCourseCode().equals("MA301"));
                if (takesCourse) {
                    enrollmentDao.enroll(student.getStudentId(), course.getCourseId());
                    enrollmentCount++;
                }
            }
        }

        AttendanceDAO attendanceDao = new AttendanceDAO();
        int attendanceCount = 0;
        for (Course course : courses) {
            List<Student> enrolled = studentsForCourse(students, enrollmentDao, course);
            LocalDate date = LocalDate.of(2026, 7, 13);
            for (int classNo = 0; classNo < 28; classNo++) {
                List<AttendanceStatus> sheet = new ArrayList<>();
                for (Student student : enrolled) {
                    sheet.add(randomStatus(random, student.getRollNumber()));
                }
                for (int i = 0; i < enrolled.size(); i++) {
                    attendanceDao.mark(enrolled.get(i).getStudentId(),
                            course.getCourseId(), date, sheet.get(i), "");
                    attendanceCount++;
                }
                date = date.plusDays(3);
            }
        }

        GradeDAO gradeDao = new GradeDAO();
        int gradeCount = 0;
        for (Course course : courses) {
            List<Student> enrolled = studentsForCourse(students, enrollmentDao, course);
            for (Student student : enrolled) {
                for (AssessmentType assessment : AssessmentType.values()) {
                    double max = assessment.getDefaultMaxMarks();
                    double marks = marksFor(random, assessment);
                    try {
                        gradeDao.save(new Grade(student.getStudentId(),
                                course.getCourseId(), assessment, marks, max,
                                assessment.getDefaultWeightPercent(), LocalDate.now()));
                        gradeCount++;
                    } catch (ValidationException e) {
                        throw new DatabaseException("Seed data validation failed.", e);
                    }
                }
            }
        }

        System.out.printf("[SeedDemoData] Done: %d students, %d courses, %d enrolments, "
                        + "%d attendance records, %d grades.%n",
                students.size(), courses.size(), enrollmentCount, attendanceCount, gradeCount);
    }

    private static List<Student> createStudents(StudentDAO studentDao) {
        String[][] raw = {
                {"CSE21001", "Aarav Sharma", "aarav.sharma@example.com", "9876500011", "A"},
                {"CSE21002", "Diya Patel", "diya.patel@example.com", "9876500012", "A"},
                {"CSE21003", "Rohan Mehta", "rohan.mehta@example.com", "9876500013", "A"},
                {"CSE21004", "Sneha Reddy", "sneha.reddy@example.com", "9876500014", "A"},
                {"CSE21005", "Arjun Nair", "arjun.nair@example.com", "9876500015", "B"},
                {"CSE21006", "Priya Iyer", "priya.iyer@example.com", "9876500016", "B"},
                {"CSE21007", "Karan Gupta", "karan.gupta@example.com", "9876500017", "A"},
                {"CSE21008", "Ananya Singh", "ananya.singh@example.com", "9876500018", "B"},
                {"CSE21009", "Rahul Verma", "rahul.verma@example.com", "9876500019", "A"},
                {"CSE21010", "Ishita Bose", "ishita.bose@example.com", "9876500020", "B"},
                {"CSE21011", "Vikram Rao", "vikram.rao@example.com", "9876500021", "A"},
                {"CSE21012", "Meera Pillai", "meera.pillai@example.com", "9876500022", "A"},
        };
        List<Student> students = new ArrayList<>();
        try {
            for (String[] row : raw) {
                Student student = new Student(row[0], row[1], row[2], row[3],
                        "Computer Science", 5, row[4]);
                studentDao.save(student);
                students.add(student);
            }
        } catch (ValidationException e) {
            throw new DatabaseException("Seed data validation failed.", e);
        }
        return students;
    }

    private static List<Course> createCourses(CourseDAO courseDao) {
        String[][] raw = {
                {"CS301", "Data Structures", "4", "Dr. Kavita Menon"},
                {"CS302", "Database Management Systems", "4", "Prof. Suresh Kumar"},
                {"CS303", "Software Engineering", "3", "Dr. Anita Desai"},
                {"MA301", "Discrete Mathematics", "3", "Prof. R. Venkatesh"},
        };
        List<Course> courses = new ArrayList<>();
        try {
            for (String[] row : raw) {
                Course course = new Course(row[0], row[1],
                        Integer.parseInt(row[2]), row[3], 5);
                courseDao.save(course);
                courses.add(course);
            }
        } catch (ValidationException e) {
            throw new DatabaseException("Seed data validation failed.", e);
        }
        return courses;
    }

    private static List<Student> studentsForCourse(List<Student> students,
                                                   EnrollmentDAO enrollmentDao,
                                                   Course course) {
        List<Student> enrolled = new ArrayList<>();
        for (Student student : students) {
            if (enrollmentDao.isEnrolled(student.getStudentId(), course.getCourseId())) {
                enrolled.add(student);
            }
        }
        return enrolled;
    }

    /** ~93% present, a few absent/late; two rolls are deliberately weaker. */
    private static AttendanceStatus randomStatus(Random random, String rollNumber) {
        boolean weakStudent = rollNumber.equals("CSE21007") || rollNumber.equals("CSE21009");
        double value = random.nextDouble();
        if (weakStudent) {
            return value < 0.60 ? AttendanceStatus.PRESENT
                    : value < 0.95 ? AttendanceStatus.ABSENT : AttendanceStatus.LATE;
        }
        return value < 0.93 ? AttendanceStatus.PRESENT
                : value < 0.985 ? AttendanceStatus.ABSENT : AttendanceStatus.LATE;
    }

    private static double marksFor(Random random, AssessmentType assessment) {
        double max = assessment.getDefaultMaxMarks();
        double base;
        switch (assessment) {
            case QUIZ, ASSIGNMENT -> base = max * 0.55;
            case MIDTERM, PROJECT -> base = max * 0.55;
            default -> base = max * 0.50;
        }
        double marks = base + random.nextDouble() * (max * 0.40);
        return Math.min(max, Math.round(marks * 10.0) / 10.0);
    }
}
