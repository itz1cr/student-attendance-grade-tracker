package com.aat.tracker.service;

import com.aat.tracker.dao.StudentDAO;
import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.model.AttendanceStat;
import com.aat.tracker.model.GradeSheetRow;
import com.aat.tracker.model.Student;
import com.aat.tracker.util.CsvExporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * CSV report generation (Week 8: File I/O; Week 10: test results and
 * documentation exports). Uses the generic {@link CsvExporter} for regular
 * reports and direct buffered I/O for the transcript.
 */
public class ReportService {

    /** Folder where exported reports are stored by default. */
    public static final String DEFAULT_EXPORT_DIR = "reports";

    private static final String ATTENDANCE_HEADER =
            "Roll Number,Student Name,Course,Present,Absent,Late,Total Classes,Attendance %";

    private static final String GRADE_SHEET_HEADER =
            "Roll Number,Student Name,Quiz %,Assignment %,Midterm %,Final %,Project %,Overall %,Letter Grade";

    private static final String STUDENT_HEADER =
            "Roll Number,Name,Email,Phone,Department,Semester,Section";

    private final AttendanceService attendanceService = new AttendanceService();
    private final GradeService gradeService = new GradeService();
    private final StudentDAO studentDao = new StudentDAO();

    private final CsvExporter<AttendanceStat> attendanceExporter =
            new CsvExporter<>(ATTENDANCE_HEADER);

    private final CsvExporter<GradeSheetRow> gradeSheetExporter =
            new CsvExporter<>(GRADE_SHEET_HEADER);

    private final CsvExporter<Student> studentExporter =
            new CsvExporter<>(STUDENT_HEADER);

    /** Attendance summary of every (student, course) pair. */
    public Path exportAttendanceReport(Path target) throws IOException {
        return attendanceExporter.export(target, attendanceService.getAllStatistics());
    }

    /** Full grade sheet of one course. */
    public Path exportGradeSheet(Path target, int courseId) throws IOException {
        return gradeSheetExporter.export(target, gradeService.getGradeSheet(courseId));
    }

    /** List of all students. */
    public Path exportStudents(Path target) throws IOException {
        return studentExporter.export(target, studentDao.findAll());
    }

    /** Personal transcript with a preamble and the semester GPA. */
    public Path exportTranscript(Path target, int studentId) throws IOException {
        Student student = studentDao.findById(studentId)
                .orElseThrow(() -> new DatabaseException("Student id " + studentId + " not found."));
        List<GradeSheetRow> rows = gradeService.getTranscript(studentId);
        double gpa = gradeService.getGpa(studentId);

        Path absolute = target.toAbsolutePath();
        if (absolute.getParent() != null) {
            Files.createDirectories(absolute.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(absolute, StandardCharsets.UTF_8)) {
            writer.write("Transcript for: " + student.getRollNumber() + " - " + student.getName());
            writer.newLine();
            writer.write(String.format(Locale.ROOT, "Semester GPA (10-point scale): %.2f", gpa));
            writer.newLine();
            writer.write(CsvExporter.toCsvLine(GRADE_SHEET_HEADER.split(",")));
            writer.newLine();
            for (GradeSheetRow row : rows) {
                writer.write(CsvExporter.toCsvLine(row.toCsvRow()));
                writer.newLine();
            }
        }
        return absolute;
    }
}
