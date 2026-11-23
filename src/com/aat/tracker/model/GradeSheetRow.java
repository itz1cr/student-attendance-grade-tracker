package com.aat.tracker.model;

import java.util.Locale;
import java.util.Map;

/**
 * One row of a course grade sheet or personal transcript: the course code,
 * per-assessment percentages, the weighted overall percentage and the
 * resulting letter grade (Java record implementing {@link Reportable}).
 */
public record GradeSheetRow(int studentId, String rollNumber, String studentName,
                            String courseCode, Map<AssessmentType, Double> assessmentPercentages,
                            double overallPercentage, GradeLetter letter)
        implements Reportable {

    @Override
    public String[] toCsvRow() {
        return new String[]{
                rollNumber,
                studentName,
                fmt(assessmentPercentages.get(AssessmentType.QUIZ)),
                fmt(assessmentPercentages.get(AssessmentType.ASSIGNMENT)),
                fmt(assessmentPercentages.get(AssessmentType.MIDTERM)),
                fmt(assessmentPercentages.get(AssessmentType.FINAL)),
                fmt(assessmentPercentages.get(AssessmentType.PROJECT)),
                String.format(Locale.ROOT, "%.1f", overallPercentage),
                letter.getLabel()
        };
    }

    /** "45.0" for a known value, "-" when the assessment is not yet recorded. */
    private static String fmt(Double value) {
        return value == null ? "-" : String.format(Locale.ROOT, "%.1f", value);
    }
}
