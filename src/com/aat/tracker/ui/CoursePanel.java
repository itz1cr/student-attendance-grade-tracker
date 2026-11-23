package com.aat.tracker.ui;

import com.aat.tracker.dao.CourseDAO;
import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.model.Course;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Courses screen (Week 6): live search, add, edit and delete.
 */
public class CoursePanel extends JPanel {

    private final CourseDAO courseDao = new CourseDAO();
    private final CourseTableModel tableModel = new CourseTableModel();
    private final JTable table = new JTable(tableModel);
    private final javax.swing.JTextField searchField = UiHelper.textField(18);

    public CoursePanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(UiHelper.DEFAULT_PADDING,
                UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(UiHelper.button("Add Course", event -> addCourse()));
        toolbar.add(UiHelper.button("Edit", event -> editSelected()));
        toolbar.add(UiHelper.button("Delete", event -> deleteSelected()));
        toolbar.add(UiHelper.button("Refresh", event -> reload()));
        add(toolbar, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        UiHelper.setColumnWidths(table, new double[]{0.15, 0.35, 0.10, 0.25, 0.15});
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                reload();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                reload();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                reload();
            }
        });

        reload();
    }

    private void reload() {
        try {
            String keyword = searchField.getText().trim();
            List<Course> courses = keyword.isEmpty()
                    ? courseDao.findAll()
                    : courseDao.search(keyword);
            tableModel.setCourses(courses);
        } catch (DatabaseException e) {
            UiHelper.showError(this, "Could not load courses: " + e.getMessage());
        }
    }

    private void addCourse() {
        CourseDialog dialog = new CourseDialog(null, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                courseDao.save(dialog.getCourse());
                reload();
                UiHelper.showInfo(this, "Course added successfully.");
            } catch (DatabaseException e) {
                UiHelper.showError(this, "Could not add the course: " + e.getMessage());
            }
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a course first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Course selected = tableModel.getCourseAt(table.convertRowIndexToModel(row));
        CourseDialog dialog = new CourseDialog(null, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                courseDao.save(dialog.getCourse());
                reload();
                UiHelper.showInfo(this, "Course updated successfully.");
            } catch (DatabaseException e) {
                UiHelper.showError(this, "Could not update the course: " + e.getMessage());
            }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a course first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Course selected = tableModel.getCourseAt(table.convertRowIndexToModel(row));
        if (!UiHelper.confirm(this, "Delete course " + selected.getCourseCode()
                + "?\n\nThis also deletes its enrolments, attendance and grades.")) {
            return;
        }
        try {
            courseDao.deleteById(selected.getCourseId());
            reload();
            UiHelper.showInfo(this, "Course deleted.");
        } catch (DatabaseException e) {
            UiHelper.showError(this, "Could not delete the course: " + e.getMessage());
        }
    }

    /** Table model backed by a {@code List<Course>} (Week 8: collections). */
    private static final class CourseTableModel extends AbstractTableModel {

        private static final String[] COLUMNS =
                {"Code", "Title", "Credits", "Instructor", "Semester"};

        private final List<Course> rows = new ArrayList<>();

        void setCourses(List<Course> courses) {
            rows.clear();
            rows.addAll(courses);
            fireTableDataChanged();
        }

        Course getCourseAt(int index) {
            return rows.get(index);
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
            Course course = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> course.getCourseCode();
                case 1 -> course.getTitle();
                case 2 -> course.getCredits();
                case 3 -> course.getInstructor();
                case 4 -> course.getSemester();
                default -> "";
            };
        }
    }
}
