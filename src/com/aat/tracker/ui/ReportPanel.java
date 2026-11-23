package com.aat.tracker.ui;

import com.aat.tracker.dao.CourseDAO;
import com.aat.tracker.dao.StudentDAO;
import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.model.Course;
import com.aat.tracker.model.Student;
import com.aat.tracker.service.ReportService;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reports screen (Week 8: File I/O; Week 10: test results and exports).
 * Exports CSV reports through the {@link ReportService} and previews the
 * generated files.
 */
public class ReportPanel extends JPanel {

    private final ReportService reportService = new ReportService();
    private final CourseDAO courseDao = new CourseDAO();
    private final StudentDAO studentDao = new StudentDAO();

    private final JComboBox<Course> courseCombo = new JComboBox<>();
    private final JComboBox<Student> studentCombo = new JComboBox<>();
    private final JTextArea preview = new JTextArea(16, 90);

    public ReportPanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(UiHelper.DEFAULT_PADDING,
                UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.add(UiHelper.button("Attendance Report (CSV)...", event -> exportAttendance()));
        toolbar.add(new JLabel("Course:"));
        toolbar.add(courseCombo);
        toolbar.add(UiHelper.button("Grade Sheet (CSV)...", event -> exportGradeSheet()));
        toolbar.add(new JLabel("Student:"));
        toolbar.add(studentCombo);
        toolbar.add(UiHelper.button("Transcript (CSV)...", event -> exportTranscript()));
        toolbar.add(UiHelper.button("Students (CSV)...", event -> exportStudents()));
        add(toolbar, BorderLayout.NORTH);

        preview.setEditable(false);
        preview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        preview.setText("Export a report to see a preview here.\n"
                + "Reports are written as CSV files that open in Excel.");
        add(new JScrollPane(preview), BorderLayout.CENTER);

        reloadCombos();
    }

    private void reloadCombos() {
        courseCombo.setModel(new DefaultComboBoxModel<>(
                courseDao.findAll().toArray(new Course[0])));
        studentCombo.setModel(new DefaultComboBoxModel<>(
                studentDao.findAll().toArray(new Student[0])));
    }

    /** Opens a save dialog; null when the user cancels. */
    private Path chooseTarget(String suggestedName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save report");
        chooser.setSelectedFile(new java.io.File(suggestedName));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return chooser.getSelectedFile().toPath();
    }

    private void exportAttendance() {
        Path target = chooseTarget("attendance_report.csv");
        if (target == null) {
            return;
        }
        try {
            Path written = reportService.exportAttendanceReport(target);
            previewWritten(written);
        } catch (IOException | DatabaseException e) {
            UiHelper.showError(this, "Export failed: " + e.getMessage());
        }
    }

    private void exportGradeSheet() {
        Course course = (Course) courseCombo.getSelectedItem();
        if (course == null) {
            UiHelper.showError(this, "Select a course first.");
            return;
        }
        Path target = chooseTarget("grade_sheet_" + course.getCourseCode() + ".csv");
        if (target == null) {
            return;
        }
        try {
            Path written = reportService.exportGradeSheet(target, course.getCourseId());
            previewWritten(written);
        } catch (IOException | DatabaseException e) {
            UiHelper.showError(this, "Export failed: " + e.getMessage());
        }
    }

    private void exportTranscript() {
        Student student = (Student) studentCombo.getSelectedItem();
        if (student == null) {
            UiHelper.showError(this, "Select a student first.");
            return;
        }
        Path target = chooseTarget("transcript_" + student.getRollNumber() + ".csv");
        if (target == null) {
            return;
        }
        try {
            Path written = reportService.exportTranscript(target, student.getStudentId());
            previewWritten(written);
        } catch (IOException | DatabaseException e) {
            UiHelper.showError(this, "Export failed: " + e.getMessage());
        }
    }

    private void exportStudents() {
        Path target = chooseTarget("students.csv");
        if (target == null) {
            return;
        }
        try {
            Path written = reportService.exportStudents(target);
            previewWritten(written);
        } catch (IOException | DatabaseException e) {
            UiHelper.showError(this, "Export failed: " + e.getMessage());
        }
    }

    /** Shows the first lines of the exported file (File I/O). */
    private void previewWritten(Path written) {
        try {
            List<String> lines = Files.readAllLines(written);
            StringBuilder builder = new StringBuilder();
            builder.append("Exported: ").append(written.toAbsolutePath()).append('\n');
            builder.append("Lines: ").append(lines.size()).append('\n');
            builder.append("------------------------------------------------------------\n");
            int shown = Math.min(lines.size(), 20);
            for (int i = 0; i < shown; i++) {
                builder.append(lines.get(i)).append('\n');
            }
            if (lines.size() > shown) {
                builder.append("... (").append(lines.size() - shown).append(" more rows)\n");
            }
            preview.setText(builder.toString());

            if (UiHelper.confirm(this, "Report exported to:\n"
                    + written.toAbsolutePath() + "\n\nOpen the file now?")) {
                Desktop.getDesktop().open(written.toFile());
            }
        } catch (IOException e) {
            UiHelper.showError(this, "Could not read the exported file: " + e.getMessage());
        }
    }
}
