package com.aat.tracker.ui;

import com.aat.tracker.dao.AttendanceDAO;
import com.aat.tracker.dao.CourseDAO;
import com.aat.tracker.dao.EnrollmentDAO;
import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.AttendanceEntry;
import com.aat.tracker.model.AttendanceStatus;
import com.aat.tracker.model.Course;
import com.aat.tracker.model.Enrollment;
import com.aat.tracker.model.Student;
import com.aat.tracker.service.AttendanceService;
import com.aat.tracker.util.Validator;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Attendance screen (Week 6: tables, combo-box editors, event handling,
 * input validation). The teacher picks a course and a date, sees every
 * enrolled student with an editable status column, and saves the whole
 * class sheet in one transactional bulk operation.
 */
public class AttendancePanel extends JPanel {

    private final CourseDAO courseDao = new CourseDAO();
    private final EnrollmentDAO enrollmentDao = new EnrollmentDAO();
    private final AttendanceDAO attendanceDao = new AttendanceDAO();
    private final AttendanceService attendanceService = new AttendanceService();

    private final JComboBox<Course> courseCombo = new JComboBox<>();
    private final JTextField dateField = UiHelper.textField(10);
    private final AttendanceTableModel tableModel = new AttendanceTableModel();
    private final JTable table = new JTable(tableModel);
    private final JLabel summaryLabel = new JLabel(" ");

    public AttendancePanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(UiHelper.DEFAULT_PADDING,
                UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.add(new JLabel("Course:"));
        toolbar.add(courseCombo);
        toolbar.add(new JLabel("Class date (yyyy-mm-dd):"));
        toolbar.add(dateField);
        toolbar.add(UiHelper.button("Load", event -> load()));
        toolbar.add(UiHelper.button("Mark All Present", event -> markAll(AttendanceStatus.PRESENT)));
        toolbar.add(UiHelper.button("Mark All Absent", event -> markAll(AttendanceStatus.ABSENT)));
        toolbar.add(UiHelper.button("Save Sheet", event -> saveSheet()));
        toolbar.add(UiHelper.button("Clear Saved Date", event -> clearSavedDate()));
        add(toolbar, BorderLayout.NORTH);

        // Editable status column: a combo box inside the table cell.
        JComboBox<AttendanceStatus> statusCombo =
                new JComboBox<>(AttendanceStatus.values());
        table.getColumnModel().getColumn(AttendanceTableModel.STATUS_COLUMN)
                .setCellEditor(new DefaultCellEditor(statusCombo));
        table.setRowHeight(24);
        UiHelper.setColumnWidths(table, new double[]{0.18, 0.32, 0.2, 0.3});
        add(new JScrollPane(table), BorderLayout.CENTER);

        summaryLabel.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 2));
        add(summaryLabel, BorderLayout.SOUTH);

        loadCourses();
        dateField.setText(LocalDate.now().toString());
        load();
    }

    private void loadCourses() {
        List<Course> courses = courseDao.findAll();
        courseCombo.setModel(new DefaultComboBoxModel<>(courses.toArray(new Course[0])));
    }

    private Course selectedCourse() {
        return (Course) courseCombo.getSelectedItem();
    }

    private LocalDate selectedDate() {
        try {
            return Validator.parseDate(dateField.getText(), "Class date");
        } catch (ValidationException e) {
            UiHelper.showError(this, e.getMessage());
            return null;
        }
    }

    private void load() {
        Course course = selectedCourse();
        LocalDate date = selectedDate();
        if (course == null || date == null) {
            return;
        }
        try {
            List<Enrollment> enrollments = enrollmentDao.findByCourse(course.getCourseId());
            Map<Integer, AttendanceStatus> saved =
                    attendanceService.getStatusMapForDate(course.getCourseId(), date);
            tableModel.setRows(enrollments, saved);
            updateSummary(course, date);
        } catch (DatabaseException e) {
            UiHelper.showError(this, "Could not load the class sheet: " + e.getMessage());
        }
    }

    private void markAll(AttendanceStatus status) {
        tableModel.setAllStatuses(status);
        updateSummary(selectedCourse(), selectedDate());
    }

    private void saveSheet() {
        Course course = selectedCourse();
        LocalDate date = selectedDate();
        if (course == null || date == null) {
            return;
        }
        List<AttendanceEntry> entries = new ArrayList<>();
        for (Row row : tableModel.getRows()) {
            entries.add(new AttendanceEntry(
                    row.student.getStudentId(), row.status, row.remark));
        }
        try {
            attendanceService.markClassAttendance(course.getCourseId(), date, entries);
            UiHelper.showInfo(this, "Attendance saved for " + course.getCourseCode()
                    + " on " + date + " (" + entries.size() + " students).");
            load();
        } catch (ValidationException e) {
            UiHelper.showError(this, e.getMessage());
        } catch (DatabaseException e) {
            UiHelper.showError(this, "Saving failed: " + e.getMessage());
        }
    }

    private void clearSavedDate() {
        Course course = selectedCourse();
        LocalDate date = selectedDate();
        if (course == null || date == null) {
            return;
        }
        if (!UiHelper.confirm(this, "Delete all saved attendance for "
                + course.getCourseCode() + " on " + date + "?")) {
            return;
        }
        try {
            boolean deleted = attendanceDao.deleteByCourseAndDate(course.getCourseId(), date);
            UiHelper.showInfo(this, deleted
                    ? "Attendance records for this class were deleted."
                    : "There were no records for this class.");
            load();
        } catch (DatabaseException e) {
            UiHelper.showError(this, "Could not clear the class sheet: " + e.getMessage());
        }
    }

    private void updateSummary(Course course, LocalDate date) {
        if (course == null) {
            return;
        }
        int present = 0;
        int absent = 0;
        int late = 0;
        for (Row row : tableModel.getRows()) {
            switch (row.status) {
                case PRESENT -> present++;
                case ABSENT -> absent++;
                default -> late++;
            }
        }
        summaryLabel.setText(String.format(Locale.ROOT,
                "Class: %s on %s | Enrolled: %d | Present: %d | Absent: %d | Late: %d",
                course.getCourseCode(), date, tableModel.getRows().size(), present, absent, late));
    }

    /** One editable sheet row (nested class, Week 8). */
    private static final class Row {
        final Student student;
        AttendanceStatus status;
        String remark;

        Row(Student student, AttendanceStatus status) {
            this.student = student;
            this.status = status;
            this.remark = "";
        }
    }

    /** Table model for the class sheet (Week 5: AbstractTableModel). */
    private static final class AttendanceTableModel extends AbstractTableModel {

        static final int STATUS_COLUMN = 2;

        private static final String[] COLUMNS =
                {"Roll Number", "Student Name", "Status", "Remark"};

        private final List<Row> rows = new ArrayList<>();

        void setRows(List<Enrollment> enrollments,
                     Map<Integer, AttendanceStatus> savedStatuses) {
            rows.clear();
            for (Enrollment enrollment : enrollments) {
                AttendanceStatus status = savedStatuses.getOrDefault(
                        enrollment.getStudent().getStudentId(), AttendanceStatus.PRESENT);
                rows.add(new Row(enrollment.getStudent(), status));
            }
            fireTableDataChanged();
        }

        List<Row> getRows() {
            return rows;
        }

        void setAllStatuses(AttendanceStatus status) {
            for (Row row : rows) {
                row.status = status;
            }
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == STATUS_COLUMN || columnIndex == 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.student.getRollNumber();
                case 1 -> row.student.getName();
                case 2 -> row.status;
                case 3 -> row.remark;
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            Row row = rows.get(rowIndex);
            if (columnIndex == STATUS_COLUMN && value instanceof AttendanceStatus status) {
                row.status = status;
            } else if (columnIndex == 3) {
                row.remark = value == null ? "" : value.toString();
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == STATUS_COLUMN ? AttendanceStatus.class : String.class;
        }
    }
}
