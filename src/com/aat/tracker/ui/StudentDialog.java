package com.aat.tracker.ui;

import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.Student;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;

/**
 * Modal dialog for adding/editing a student (Week 6: forms, input
 * validation, user feedback). Invalid input is rejected by the model
 * constructors with a user-defined {@link ValidationException}, which is
 * caught here and shown to the user.
 */
public class StudentDialog extends JDialog {

    private final JTextField rollField = UiHelper.textField(15);
    private final JTextField nameField = UiHelper.textField(20);
    private final JTextField emailField = UiHelper.textField(20);
    private final JTextField phoneField = UiHelper.textField(15);
    private final JTextField departmentField = UiHelper.textField(15);
    private final JTextField sectionField = UiHelper.textField(5);
    private final JSpinner semesterSpinner = new JSpinner(
            new SpinnerNumberModel(1, Student.MIN_SEMESTER, Student.MAX_SEMESTER, 1));

    private final Student existing;
    private Student built;
    private boolean saved;

    public StudentDialog(Frame owner, Student existing) {
        super(owner, existing == null ? "Add Student" : "Edit Student", true);
        this.existing = existing;
        buildUi();
        prefill();
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    /** The validated student entered by the user; valid only after saving. */
    public Student getStudent() {
        return built;
    }

    private void buildUi() {
        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));
        form.add(new JLabel("Roll Number:", SwingConstants.RIGHT));
        form.add(rollField);
        form.add(new JLabel("Name:", SwingConstants.RIGHT));
        form.add(nameField);
        form.add(new JLabel("Email:", SwingConstants.RIGHT));
        form.add(emailField);
        form.add(new JLabel("Phone:", SwingConstants.RIGHT));
        form.add(phoneField);
        form.add(new JLabel("Department:", SwingConstants.RIGHT));
        form.add(departmentField);
        form.add(new JLabel("Semester (1-8):", SwingConstants.RIGHT));
        form.add(semesterSpinner);
        form.add(new JLabel("Section:", SwingConstants.RIGHT));
        form.add(sectionField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(6, 14, 12, 14));
        JButton cancelButton = UiHelper.button("Cancel", event -> dispose());
        buttons.add(cancelButton);
        buttons.add(UiHelper.button("Save", event -> save()));

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void prefill() {
        if (existing != null) {
            rollField.setText(existing.getRollNumber());
            nameField.setText(existing.getName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
            departmentField.setText(existing.getDepartment());
            semesterSpinner.setValue(existing.getSemester());
            sectionField.setText(existing.getSection());
        }
    }

    private void save() {
        try {
            Student student = new Student(
                    rollField.getText(),
                    nameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    departmentField.getText(),
                    (Integer) semesterSpinner.getValue(),
                    sectionField.getText());
            if (existing != null) {
                student.setStudentId(existing.getStudentId());
            }
            built = student;
            saved = true;
            dispose();
        } catch (ValidationException e) {
            // Built-in exception handling: show the rule violation to the user.
            UiHelper.showError(this, e.getMessage());
        }
    }
}
