package com.aat.tracker.ui;

import com.aat.tracker.dao.StudentDAO;
import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.Student;

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
 * Students screen (Week 6: forms/tables, navigation, validation, user
 * feedback). Supports live search, add, edit and delete.
 */
public class StudentPanel extends JPanel {

    private final StudentDAO studentDao = new StudentDAO();
    private final StudentTableModel tableModel = new StudentTableModel();
    private final JTable table = new JTable(tableModel);
    private final javax.swing.JTextField searchField = UiHelper.textField(18);

    public StudentPanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(UiHelper.DEFAULT_PADDING,
                UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(UiHelper.button("Add Student", event -> addStudent()));
        toolbar.add(UiHelper.button("Edit", event -> editSelected()));
        toolbar.add(UiHelper.button("Delete", event -> deleteSelected()));
        toolbar.add(UiHelper.button("Refresh", event -> reload()));
        add(toolbar, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        UiHelper.setColumnWidths(table, new double[]{0.13, 0.24, 0.27, 0.12, 0.12, 0.06, 0.06});
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Live search: every keystroke re-queries the database.
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
            List<Student> students = keyword.isEmpty()
                    ? studentDao.findAll()
                    : studentDao.search(keyword);
            tableModel.setStudents(students);
        } catch (DatabaseException e) {
            UiHelper.showError(this, "Could not load students: " + e.getMessage());
        }
    }

    private void addStudent() {
        StudentDialog dialog = new StudentDialog(null, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                studentDao.save(dialog.getStudent());
                reload();
                UiHelper.showInfo(this, "Student added successfully.");
            } catch (DatabaseException e) {
                // e.g. duplicate roll number -> UNIQUE constraint violation.
                UiHelper.showError(this, "Could not add the student: " + e.getMessage());
            }
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Student selected = tableModel.getStudentAt(table.convertRowIndexToModel(row));
        StudentDialog dialog = new StudentDialog(null, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                studentDao.save(dialog.getStudent());
                reload();
                UiHelper.showInfo(this, "Student updated successfully.");
            } catch (DatabaseException e) {
                UiHelper.showError(this, "Could not update the student: " + e.getMessage());
            }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Student selected = tableModel.getStudentAt(table.convertRowIndexToModel(row));
        if (!UiHelper.confirm(this, "Delete student " + selected.getRollNumber()
                + "?\n\nThis also deletes the student's enrolments, attendance and grades.")) {
            return;
        }
        try {
            studentDao.deleteById(selected.getStudentId());
            reload();
            UiHelper.showInfo(this, "Student deleted.");
        } catch (DatabaseException e) {
            UiHelper.showError(this, "Could not delete the student: " + e.getMessage());
        }
    }

    /**
     * Table model backed by a {@code List<Student>}
     * (Week 5: inheritance from AbstractTableModel; Week 8: collections).
     */
    private static final class StudentTableModel extends AbstractTableModel {

        private static final String[] COLUMNS =
                {"Roll Number", "Name", "Email", "Phone", "Department", "Semester", "Section"};

        private final List<Student> rows = new ArrayList<>();

        void setStudents(List<Student> students) {
            rows.clear();
            rows.addAll(students);
            fireTableDataChanged();
        }

        Student getStudentAt(int index) {
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
            Student student = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> student.getRollNumber();
                case 1 -> student.getName();
                case 2 -> student.getEmail();
                case 3 -> student.getPhone();
                case 4 -> student.getDepartment();
                case 5 -> student.getSemester();
                case 6 -> student.getSection();
                default -> "";
            };
        }
    }
}
