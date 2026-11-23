package com.aat.tracker.model;

/**
 * Letter grade with grade points (10-point scale) and the minimum percentage
 * required to obtain it (Week 8: enumerations).
 */
public enum GradeLetter {

    A_PLUS("A+", 10.0, 90.0),
    A("A", 9.0, 80.0),
    B_PLUS("B+", 8.0, 70.0),
    B("B", 7.0, 60.0),
    C("C", 6.0, 50.0),
    D("D", 5.0, 40.0),
    F("F", 0.0, 0.0);

    private final String label;
    private final double gradePoints;
    private final double minPercentage;

    GradeLetter(String label, double gradePoints, double minPercentage) {
        this.label = label;
        this.gradePoints = gradePoints;
        this.minPercentage = minPercentage;
    }

    public String getLabel() {
        return label;
    }

    public double getGradePoints() {
        return gradePoints;
    }

    public double getMinPercentage() {
        return minPercentage;
    }

    /**
     * Static factory that maps a percentage to the correct letter grade
     * (Week 4: static methods, Week 8: enum iteration).
     */
    public static GradeLetter fromPercentage(double percentage) {
        for (GradeLetter letter : values()) {
            if (percentage >= letter.minPercentage) {
                return letter;
            }
        }
        return F;
    }

    @Override
    public String toString() {
        return label;
    }
}
