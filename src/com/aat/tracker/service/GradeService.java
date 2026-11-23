package com.aat.tracker.service;

import com.aat.tracker.dao.EnrollmentDAO;
import com.aat.tracker.dao.GradeDAO;
import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.AssessmentType;
import com.aat.tracker.model.Course;
import com.aat.tracker.model.Enrollment;
import com.aat.tracker.model.Grade;
import com.aat.tracker.model.GradeLetter;
import com.aat.tracker.model.GradeSheetRow;
import com.aat.tracker.model.Student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules around grades: validation, weighted percentages, letter
 * grades, grade sheets and GPA on the 10-point scale.
 */
public class GradeService {

    private final GradeDAO gradeDao = new GradeDAO();
    private final EnrollmentDAO enrollmentDao = new EnrollmentDAO();

    /** Saves one grade after enforcing the business rules. */
    public void saveGrade(Grade grade) throws ValidationException {
        if (grade.getMarksObtained() > grade.getMaxMarks()) {
            throw new ValidationException("Marks obtained cannot exceed the maximum marks.");
        }
        if (!enrollmentDao.isEnrolled(grade.getStudentId(), grade.getCourseId())) {
            throw new ValidationException("The student is not enrolled in this course.");
        }
        gradeDao.save(grade);
    }

    /**
     * Weighted overall percentage of a set of assessments. Normalised by the
     * recorded weights, so partial records (e.g. only the quiz so far) still
     * produce a correct percentage.
     */
    public static double weightedPercentage(List<Grade> grades) {
        double totalWeight = 0.0;
        double weightedSum = 0.0;
        for (Grade grade : grades) {
            totalWeight += grade.getWeightPercent();
            weightedSum += grade.percentage() * grade.getWeightPercent();
        }
        return totalWeight <= 0 ? 0.0 : weightedSum / totalWeight;
    }

    /** Grade sheet of one course: one row per enrolled student, sorted by rank. */
    public List<GradeSheetRow> getGradeSheet(int courseId) {
        List<Enrollment> enrollments = enrollmentDao.findByCourse(courseId);
        List<GradeSheetRow> rows = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            rows.add(buildRow(enrollment.getStudent(), enrollment.getCourse()));
        }
        rows.sort(Comparator.comparingDouble(GradeSheetRow::overallPercentage).reversed());
        return rows;
    }

    /** Transcript of one student: one row per enrolled course, sorted by rank. */
    public List<GradeSheetRow> getTranscript(int studentId) {
        List<Enrollment> enrollments = enrollmentDao.findByStudent(studentId);
        List<GradeSheetRow> rows = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            rows.add(buildRow(enrollment.getStudent(), enrollment.getCourse()));
        }
        rows.sort(Comparator.comparingDouble(GradeSheetRow::overallPercentage).reversed());
        return rows;
    }

    /**
     * GPA on the 10-point scale: grade points weighted by course credits
     * over every course the student is enrolled in.
     */
    public double getGpa(int studentId) {
        double totalPoints = 0.0;
        int totalCredits = 0;
        // Build the GPA directly from the enrolled courses to keep credits aligned.
        for (Enrollment enrollment : enrollmentDao.findByStudent(studentId)) {
            Course course = enrollment.getCourse();
            List<Grade> grades = gradeDao.findByStudentAndCourse(studentId, course.getCourseId());
            double percentage = weightedPercentage(grades);
            if (percentage > 0) {
                double points = GradeLetter.fromPercentage(percentage).getGradePoints();
                totalPoints += points * course.getCredits();
                totalCredits += course.getCredits();
            }
        }
        return totalCredits == 0 ? 0.0 : totalPoints / totalCredits;
    }

    /** Grades of one student in one course. */
    public List<Grade> getGrades(int studentId, int courseId) {
        return gradeDao.findByStudentAndCourse(studentId, courseId);
    }

    private GradeSheetRow buildRow(Student student, Course course) {
        List<Grade> grades = gradeDao.findByStudentAndCourse(
                student.getStudentId(), course.getCourseId());
        Map<AssessmentType, Double> percentages = new EnumMap<>(AssessmentType.class);
        for (Grade grade : grades) {
            percentages.put(grade.getAssessment(), grade.percentage());
        }
        double overall = weightedPercentage(grades);
        return new GradeSheetRow(student.getStudentId(), student.getRollNumber(),
                student.getName(), course.getCourseCode(), percentages, overall,
                GradeLetter.fromPercentage(overall));
    }
}
