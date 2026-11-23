package com.aat.tracker.model;

import com.aat.tracker.exception.ValidationException;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Marks obtained by one student in one assessment of one course.
 */
public class Grade implements Reportable {

    public static final int NEW_GRADE_ID = -1;

    private int gradeId;
    private final int studentId;
    private final int courseId;
    private final AssessmentType assessment;
    private double marksObtained;
    private double maxMarks;
    private double weightPercent;
    private LocalDate recordedOn;

    /** Full constructor. */
    public Grade(int studentId, int courseId, AssessmentType assessment,
                 double marksObtained, double maxMarks, double weightPercent,
                 LocalDate recordedOn) throws ValidationException {
        this.gradeId = NEW_GRADE_ID;
        this.studentId = studentId;
        this.courseId = courseId;
        this.assessment = assessment;
        setMarksObtained(marksObtained);
        setMaxMarks(maxMarks);
        setWeightPercent(weightPercent);
        this.recordedOn = recordedOn == null ? LocalDate.now() : recordedOn;
    }

    /** Overloaded constructor using the assessment's default maximum marks and weight. */
    public Grade(int studentId, int courseId, AssessmentType assessment, double marksObtained)
            throws ValidationException {
        this(studentId, courseId, assessment, marksObtained,
                assessment.getDefaultMaxMarks(), assessment.getDefaultWeightPercent(),
                LocalDate.now());
    }

    /** Overloaded constructor with a custom maximum but default weight. */
    public Grade(int studentId, int courseId, AssessmentType assessment,
                 double marksObtained, double maxMarks) throws ValidationException {
        this(studentId, courseId, assessment, marksObtained, maxMarks,
                assessment.getDefaultWeightPercent(), LocalDate.now());
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public AssessmentType getAssessment() {
        return assessment;
    }

    public double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(double marksObtained) throws ValidationException {
        if (marksObtained < 0) {
            throw new ValidationException("Marks cannot be negative.");
        }
        this.marksObtained = marksObtained;
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) throws ValidationException {
        if (maxMarks <= 0) {
            throw new ValidationException("Maximum marks must be greater than zero.");
        }
        this.maxMarks = maxMarks;
    }

    public double getWeightPercent() {
        return weightPercent;
    }

    public void setWeightPercent(double weightPercent) throws ValidationException {
        if (weightPercent < 0 || weightPercent > 100) {
            throw new ValidationException("Weight must be between 0 and 100.");
        }
        this.weightPercent = weightPercent;
    }

    public LocalDate getRecordedOn() {
        return recordedOn;
    }

    public void setRecordedOn(LocalDate recordedOn) {
        this.recordedOn = recordedOn;
    }

    /** Percentage scored in this assessment. */
    public double percentage() {
        return maxMarks > 0 ? (marksObtained / maxMarks) * 100.0 : 0.0;
    }

    /** Percentage scaled by this assessment's weight. */
    public double weightedScore() {
        return percentage() * weightPercent / 100.0;
    }

    /** Letter grade for this assessment alone. */
    public GradeLetter letter() {
        return GradeLetter.fromPercentage(percentage());
    }

    @Override
    public String[] toCsvRow() {
        return new String[]{
                String.valueOf(studentId),
                String.valueOf(courseId),
                assessment.name(),
                String.format(Locale.ROOT, "%.1f", marksObtained),
                String.format(Locale.ROOT, "%.1f", maxMarks),
                String.format(Locale.ROOT, "%.1f", weightPercent),
                String.valueOf(recordedOn)
        };
    }

    @Override
    public String toString() {
        return assessment + ": " + marksObtained + "/" + maxMarks
                + " (" + String.format(Locale.ROOT, "%.1f", percentage()) + "%)";
    }
}
