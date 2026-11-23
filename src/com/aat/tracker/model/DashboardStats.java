package com.aat.tracker.model;

/**
 * Aggregated figures shown on the dashboard (Java record).
 */
public record DashboardStats(int totalStudents, int totalCourses, int totalEnrollments,
                             int totalAttendanceRecords, int totalGradeRecords,
                             double averageAttendancePercent, int lowAttendanceCount,
                             double averageGpa) {
}
