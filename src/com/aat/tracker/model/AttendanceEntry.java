package com.aat.tracker.model;

/**
 * One row of the bulk attendance sheet: the status to record for one
 * student in one class (Java record).
 */
public record AttendanceEntry(int studentId, AttendanceStatus status, String remark) {
}
