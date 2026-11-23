package com.aat.tracker.model;

import java.util.Locale;

/**
 * Aggregated attendance figures for one student in one course
 * (Java record: concise immutable data carrier, JDK 16+).
 */
public record AttendanceStat(int studentId, String rollNumber, String studentName,
                             String courseCode, int present, int absent, int late,
                             int totalClasses, double attendancePercentage)
        implements Reportable {

    /** True when the student has fallen below the given attendance threshold. */
    public boolean isBelowThreshold(double threshold) {
        return attendancePercentage < threshold;
    }

    @Override
    public String[] toCsvRow() {
        return new String[]{
                rollNumber,
                studentName,
                courseCode,
                String.valueOf(present),
                String.valueOf(absent),
                String.valueOf(late),
                String.valueOf(totalClasses),
                String.format(Locale.ROOT, "%.1f", attendancePercentage)
        };
    }
}
