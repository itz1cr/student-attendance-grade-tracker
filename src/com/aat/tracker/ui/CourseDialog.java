package com.aat.tracker.ui;

import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.Course;

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
 * Modal dialog for adding/editing a course (Week 6: forms and validation).
 */
public class CourseDialog extends JDialog {

    private final JTextField codeField = UiHelper.textField(10);
    private final JTextField titleField = UiHelper.textField(25);
    private final JSpinner creditsSpinner = new JSpinner(
            new SpinnerNumberModel(Course.DEFAULT_CREDITS, Course.MIN_CREDITS, Course.MAX_CREDITS, 1));
    private final JTextField instructorField = UiHelper.textField(20);
    private final JSpinner semesterSpinner = new JSpinner(
            new SpinnerNumberModel(1, StudentSemesters.MIN, StudentSemesters.MAX, 1));

    private final Course existing;
    private Course built;
    private boolean saved;

    public CourseDialog(Frame owner, Course existing) {
        super(owner, existing == null ? "Add Course" : "Edit Course", true);
        this.existing = existing;
        buildUi();
        prefill();
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    public Course getCourse() {
        return built;
    }

    private void buildUi() {
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));
        form.add(new JLabel("Course Code:", SwingConstants.RIGHT));
        form.add(codeField);
        form.add(new JLabel("Title:", SwingConstants.RIGHT));
        form.add(titleField);
        form.add(new JLabel("Credits (1-6):", SwingConstants.RIGHT));
        form.add(creditsSpinner);
        form.add(new JLabel("Instructor:", SwingConstants.RIGHT));
        form.add(instructorField);
        form.add(new JLabel("Semester (1-8):", SwingConstants.RIGHT));
        form.add(semesterSpinner);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(6, 14, 12, 14));
        buttons.add(UiHelper.button("Cancel", event -> dispose()));
        buttons.add(UiHelper.button("Save", event -> save()));

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void prefill() {
        if (existing != null) {
            codeField.setText(existing.getCourseCode());
            titleField.setText(existing.getTitle());
            creditsSpinner.setValue(existing.getCredits());
            instructorField.setText(existing.getInstructor());
            semesterSpinner.setValue(existing.getSemester());
        }
    }

    private void save() {
        try {
            Course course = new Course(
                    codeField.getText(),
                    titleField.getText(),
                    (Integer) creditsSpinner.getValue(),
                    instructorField.getText(),
                    (Integer) semesterSpinner.getValue());
            if (existing != null) {
                course.setCourseId(existing.getCourseId());
            }
            built = course;
            saved = true;
            dispose();
        } catch (ValidationException e) {
            UiHelper.showError(this, e.getMessage());
        }
    }

    /** Re-exposes the semester bounds so the dialog stays decoupled. */
    private static final class StudentSemesters {
        static final int MIN = 1;
        static final int MAX = 8;

        private StudentSemesters() {
        }
    }
}
