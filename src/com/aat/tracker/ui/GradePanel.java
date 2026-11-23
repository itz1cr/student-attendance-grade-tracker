package com.aat.tracker.ui;

import com.aat.tracker.dao.CourseDAO;
import com.aat.tracker.dao.EnrollmentDAO;
import com.aat.tracker.dao.StudentDAO;
import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.AssessmentType;
import com.aat.tracker.model.Course;
import com.aat.tracker.model.Enrollment;
import com.aat.tracker.model.Grade;
import com.aat.tracker.model.GradeSheetRow;
import com.aat.tracker.model.Student;
import com.aat.tracker.service.GradeService;
import com.aat.tracker.util.Validator;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Grades screen (Week 6: forms, tables, navigation, validation).
 *
 * <p>Three sub-screens: recording marks per assessment, the course result
 * sheet, and a personal transcript with GPA.</p>
 */
public class GradePanel extends JPanel {

    private final CourseDAO courseDao = new CourseDAO();
    private final StudentDAO studentDao = new StudentDAO();
    private final EnrollmentDAO enrollmentDao = new EnrollmentDAO();
    private final GradeService gradeService = new GradeService();

    public GradePanel() {
        super(new BorderLayout());
        JTabbedPane subTabs = new JTabbedPane();
        subTabs.addTab("Record Grades", new RecordGradesTab());
        subTabs.addTab("Result Sheet", new ResultSheetTab());
        subTabs.addTab("Transcript & GPA", new TranscriptTab());
        add(subTabs, BorderLayout.CENTER);
    }

    /** Builds the enrolled-student combo model for a course. */
    private DefaultComboBoxModel<Student> studentModelFor(int courseId) {
        List<Enrollment> enrollments = enrollmentDao.findByCourse(courseId);
        List<Student> students = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            students.add(enrollment.getStudent());
        }
        return new DefaultComboBoxModel<>(students.toArray(new Student[0]));
    }

    // ------------------------------------------------------------------
    // Tab 1: record marks for one student in one course
    // ------------------------------------------------------------------
    private final class RecordGradesTab extends JPanel {

        private final JComboBox<Course> courseCombo = new JComboBox<>();
        private final JComboBox<Student> studentCombo = new JComboBox<>();
        private final Map<AssessmentType, JTextField> markFields = new HashMap<>();
        private final JLabel courseInfoLabel = new JLabel(" ");

        RecordGradesTab() {
            super(new BorderLayout(0, 10));
            setBorder(BorderFactory.createEmptyBorder(UiHelper.DEFAULT_PADDING,
                    UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING));

            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            toolbar.add(new JLabel("Course:"));
            toolbar.add(courseCombo);
            toolbar.add(new JLabel("Student:"));
            toolbar.add(studentCombo);
            toolbar.add(UiHelper.button("Load Marks", event -> loadMarks()));
            add(toolbar, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridLayout(AssessmentType.values().length + 1, 2, 8, 8));
            for (AssessmentType assessment : AssessmentType.values()) {
                JTextField field = UiHelper.textField(8);
                markFields.put(assessment, field);
                form.add(new JLabel(String.format(Locale.ROOT, "%s (max %.0f, weight %.0f%%):",
                        assessment.getDisplayName(), assessment.getDefaultMaxMarks(),
                        assessment.getDefaultWeightPercent())));
                form.add(field);
            }
            form.add(new JLabel(" "));
            form.add(courseInfoLabel);
            add(form, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.add(UiHelper.button("Clear Fields", event -> clearFields()));
            buttons.add(UiHelper.button("Save Grades", event -> saveGrades()));
            add(buttons, BorderLayout.SOUTH);

            courseCombo.setModel(new DefaultComboBoxModel<>(
                    courseDao.findAll().toArray(new Course[0])));
            courseCombo.addActionListener(event -> reloadStudents());
            reloadStudents();
            loadMarks();
        }

        private void reloadStudents() {
            Course course = (Course) courseCombo.getSelectedItem();
            if (course != null) {
                studentCombo.setModel(studentModelFor(course.getCourseId()));
            }
        }

        private Student selectedStudent() {
            return (Student) studentCombo.getSelectedItem();
        }

        private void loadMarks() {
            Student student = selectedStudent();
            Course course = (Course) courseCombo.getSelectedItem();
            clearFields();
            if (student == null || course == null) {
                return;
            }
            try {
                List<Grade> grades = gradeService.getGrades(
                        student.getStudentId(), course.getCourseId());
                for (Grade grade : grades) {
                    JTextField field = markFields.get(grade.getAssessment());
                    if (field != null) {
                        field.setText(String.valueOf(grade.getMarksObtained()));
                    }
                }
                courseInfoLabel.setText("Entered marks for " + student.getRollNumber()
                        + " in " + course.getCourseCode());
            } catch (DatabaseException e) {
                UiHelper.showError(this, "Could not load marks: " + e.getMessage());
            }
        }

        private void clearFields() {
            for (JTextField field : markFields.values()) {
                field.setText("");
            }
        }

        private void saveGrades() {
            Student student = selectedStudent();
            Course course = (Course) courseCombo.getSelectedItem();
            if (student == null || course == null) {
                UiHelper.showError(this, "Select a course and a student first.");
                return;
            }
            try {
                int savedCount = 0;
                for (Map.Entry<AssessmentType, JTextField> entry : markFields.entrySet()) {
                    String text = entry.getValue().getText().trim();
                    if (text.isEmpty()) {
                        continue; // assessment not entered yet
                    }
                    AssessmentType assessment = entry.getKey();
                    double marks = Validator.parseMarks(text, assessment.getDisplayName());
                    Validator.requireRange(marks, 0, assessment.getDefaultMaxMarks(),
                            assessment.getDisplayName());
                    gradeService.saveGrade(new Grade(
                            student.getStudentId(), course.getCourseId(), assessment, marks));
                    savedCount++;
                }
                if (savedCount == 0) {
                    throw new ValidationException("Enter marks for at least one assessment.");
                }
                UiHelper.showInfo(this, "Saved " + savedCount + " assessment(s) for "
                        + student.getRollNumber() + " in " + course.getCourseCode() + ".");
                loadMarks();
            } catch (ValidationException e) {
                UiHelper.showError(this, e.getMessage());
            } catch (DatabaseException e) {
                UiHelper.showError(this, "Could not save grades: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // Tab 2: course result sheet
    // ------------------------------------------------------------------
    private final class ResultSheetTab extends JPanel {

        private final JComboBox<Course> courseCombo = new JComboBox<>();
        private final ResultSheetTableModel tableModel = new ResultSheetTableModel();
        private final JTable table = new JTable(tableModel);

        ResultSheetTab() {
            super(new BorderLayout(0, 10));
            setBorder(BorderFactory.createEmptyBorder(UiHelper.DEFAULT_PADDING,
                    UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING));

            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            toolbar.add(new JLabel("Course:"));
            toolbar.add(courseCombo);
            toolbar.add(UiHelper.button("Load Sheet", event -> load()));
            add(toolbar, BorderLayout.NORTH);

            table.setRowHeight(24);
            add(new JScrollPane(table), BorderLayout.CENTER);

            courseCombo.setModel(new DefaultComboBoxModel<>(
                    courseDao.findAll().toArray(new Course[0])));
            load();
        }

        private void load() {
            Course course = (Course) courseCombo.getSelectedItem();
            if (course == null) {
                return;
            }
            try {
                tableModel.setRows(gradeService.getGradeSheet(course.getCourseId()));
            } catch (DatabaseException e) {
                UiHelper.showError(this, "Could not load the result sheet: " + e.getMessage());
            }
        }
    }

    /** Result sheet table model. */
    private static final class ResultSheetTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "Roll Number", "Student", "Quiz %", "Assignment %", "Midterm %",
                "Final %", "Project %", "Overall %", "Letter"
        };

        private final List<GradeSheetRow> rows = new ArrayList<>();

        void setRows(List<GradeSheetRow> newRows) {
            rows.clear();
            rows.addAll(newRows);
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            GradeSheetRow row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.rollNumber();
                case 1 -> row.studentName();
                case 2 -> percent(row.assessmentPercentages().get(AssessmentType.QUIZ));
                case 3 -> percent(row.assessmentPercentages().get(AssessmentType.ASSIGNMENT));
                case 4 -> percent(row.assessmentPercentages().get(AssessmentType.MIDTERM));
                case 5 -> percent(row.assessmentPercentages().get(AssessmentType.FINAL));
                case 6 -> percent(row.assessmentPercentages().get(AssessmentType.PROJECT));
                case 7 -> String.format(Locale.ROOT, "%.1f", row.overallPercentage());
                case 8 -> row.letter().getLabel();
                default -> "";
            };
        }

        private static String percent(Double value) {
            return value == null ? "-" : String.format(Locale.ROOT, "%.1f", value);
        }
    }

    // ------------------------------------------------------------------
    // Tab 3: personal transcript & GPA
    // ------------------------------------------------------------------
    private final class TranscriptTab extends JPanel {

        private final JComboBox<Student> studentCombo = new JComboBox<>();
        private final TranscriptTableModel tableModel = new TranscriptTableModel();
        private final JTable table = new JTable(tableModel);
        private final JLabel gpaLabel = new JLabel("GPA: -");

        TranscriptTab() {
            super(new BorderLayout(0, 10));
            setBorder(BorderFactory.createEmptyBorder(UiHelper.DEFAULT_PADDING,
                    UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING));

            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            toolbar.add(new JLabel("Student:"));
            toolbar.add(studentCombo);
            toolbar.add(UiHelper.button("Load Transcript", event -> load()));
            toolbar.add(gpaLabel);
            add(toolbar, BorderLayout.NORTH);

            table.setRowHeight(24);
            add(new JScrollPane(table), BorderLayout.CENTER);

            studentCombo.setModel(new DefaultComboBoxModel<>(
                    studentDao.findAll().toArray(new Student[0])));
            load();
        }

        private void load() {
            Student student = (Student) studentCombo.getSelectedItem();
            if (student == null) {
                return;
            }
            try {
                List<Enrollment> enrollments = enrollmentDao.findByStudent(student.getStudentId());
                List<GradeSheetRow> rows = gradeService.getTranscript(student.getStudentId());
                Map<String, Course> courseByCode = new HashMap<>();
                for (Enrollment enrollment : enrollments) {
                    courseByCode.put(enrollment.getCourse().getCourseCode(),
                            enrollment.getCourse());
                }
                tableModel.setRows(rows, courseByCode);
                double gpa = gradeService.getGpa(student.getStudentId());
                gpaLabel.setText(String.format(Locale.ROOT, "GPA (10-point scale): %.2f", gpa));
            } catch (DatabaseException e) {
                UiHelper.showError(this, "Could not load the transcript: " + e.getMessage());
            }
        }
    }

    /** Transcript table model. */
    private static final class TranscriptTableModel extends AbstractTableModel {

        private static final String[] COLUMNS =
                {"Course", "Credits", "Overall %", "Letter", "Grade Points"};

        private final List<GradeSheetRow> rows = new ArrayList<>();
        private final List<Course> courses = new ArrayList<>();

        void setRows(List<GradeSheetRow> newRows, Map<String, Course> courseByCode) {
            rows.clear();
            courses.clear();
            for (GradeSheetRow row : newRows) {
                rows.add(row);
                courses.add(courseByCode.get(row.courseCode()));
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            GradeSheetRow row = rows.get(rowIndex);
            Course course = courses.size() > rowIndex ? courses.get(rowIndex) : null;
            return switch (columnIndex) {
                case 0 -> course == null ? "-" : course.displayName();
                case 1 -> course == null ? 0 : course.getCredits();
                case 2 -> String.format(Locale.ROOT, "%.1f", row.overallPercentage());
                case 3 -> row.letter().getLabel();
                case 4 -> String.format(Locale.ROOT, "%.1f", row.letter().getGradePoints());
                default -> "";
            };
        }
    }
}
