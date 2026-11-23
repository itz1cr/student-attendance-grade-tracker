package com.aat.tracker.model;

/**
 * Contract for every entity that can be exported as a CSV row
 * (Week 5: interfaces; Week 8: used with bounded generics in CsvExporter).
 *
 * <p>Implementations: Student, Course, AttendanceRecord, Grade,
 * AttendanceStat, GradeSheetRow.</p>
 */
public interface Reportable {

    /**
     * @return the values of one CSV row, in the exact column order of the header.
     */
    String[] toCsvRow();
}
