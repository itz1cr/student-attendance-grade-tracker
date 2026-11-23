package com.aat.tracker.model;

/**
 * Assessment components of a course with their default weightage and maximum
 * marks (Week 8: enumerations with state and behaviour).
 */
public enum AssessmentType {

    QUIZ("Quiz", 10.0, 20.0),
    ASSIGNMENT("Assignment", 10.0, 20.0),
    MIDTERM("Mid-Term Exam", 25.0, 50.0),
    FINAL("Final Exam", 40.0, 100.0),
    PROJECT("Project", 15.0, 50.0);

    private final String displayName;
    private final double defaultWeightPercent;
    private final double defaultMaxMarks;

    AssessmentType(String displayName, double defaultWeightPercent, double defaultMaxMarks) {
        this.displayName = displayName;
        this.defaultWeightPercent = defaultWeightPercent;
        this.defaultMaxMarks = defaultMaxMarks;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Default contribution of this component to the final percentage (sums to 100). */
    public double getDefaultWeightPercent() {
        return defaultWeightPercent;
    }

    public double getDefaultMaxMarks() {
        return defaultMaxMarks;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
