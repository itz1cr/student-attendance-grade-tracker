package com.aat.tracker.ui;

import com.aat.tracker.model.DashboardStats;
import com.aat.tracker.service.AttendanceService;
import com.aat.tracker.service.DashboardService;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;

/**
 * Dashboard screen: key statistics as cards plus the list of students below
 * the 75% attendance threshold.
 *
 * <p>Refreshing runs inside a {@link SwingWorker} background thread so the
 * UI never freezes (Week 8: optional multithreading).</p>
 */
public class DashboardPanel extends JPanel {

    private final JLabel studentsValue = statValue();
    private final JLabel coursesValue = statValue();
    private final JLabel enrollmentsValue = statValue();
    private final JLabel attendanceValue = statValue();
    private final JLabel gradesValue = statValue();
    private final JLabel avgAttendanceValue = statValue();
    private final JLabel lowAttendanceValue = statValue();
    private final JLabel gpaValue = statValue();

    private final DefaultTableModel lowAttendanceModel = new DefaultTableModel(
            new String[]{"Roll Number", "Student", "Course", "Attendance %"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable lowAttendanceTable = new JTable(lowAttendanceModel);
    private final JLabel statusLabel = new JLabel(" ");

    public DashboardPanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(UiHelper.DEFAULT_PADDING,
                UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING, UiHelper.DEFAULT_PADDING));

        add(buildCards(), BorderLayout.NORTH);

        JPanel lowPanel = new JPanel(new BorderLayout(0, 6));
        lowPanel.add(UiHelper.titleLabel("Students below "
                + (int) AttendanceService.MINIMUM_ATTENDANCE_PERCENT + "% attendance"), BorderLayout.NORTH);
        lowAttendanceTable.setRowHeight(24);
        UiHelper.setColumnWidths(lowAttendanceTable, new double[]{0.2, 0.4, 0.25, 0.15});
        lowPanel.add(new JScrollPane(lowAttendanceTable), BorderLayout.CENTER);
        add(lowPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(statusLabel, BorderLayout.WEST);
        bottom.add(UiHelper.button("Refresh Now",
                event -> refreshAsync()), BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        refresh(); // synchronous load so the panel is populated at startup
    }

    private JPanel buildCards() {
        JPanel cards = new JPanel(new GridLayout(2, 4, 10, 10));
        cards.add(card("Students", studentsValue));
        cards.add(card("Courses", coursesValue));
        cards.add(card("Enrolments", enrollmentsValue));
        cards.add(card("Attendance Records", attendanceValue));
        cards.add(card("Grade Records", gradesValue));
        cards.add(card("Average Attendance", avgAttendanceValue));
        cards.add(card("Below 75% (pairs)", lowAttendanceValue));
        cards.add(card("Average GPA", gpaValue));
        return cards;
    }

    private JPanel card(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private static JLabel statValue() {
        JLabel label = new JLabel("-", JLabel.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22));
        return label;
    }

    /** Synchronous refresh (startup + screenshot generator). */
    public void refresh() {
        try {
            applyStats(new DashboardService().getStats());
            statusLabel.setText("Last updated: " + java.time.LocalTime.now()
                    .truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        } catch (Exception e) {
            statusLabel.setText("Dashboard refresh failed: " + e.getMessage());
            UiHelper.showError(this, "Could not load the dashboard: " + e.getMessage());
        }
    }

    /** Asynchronous refresh via SwingWorker: background load, EDT update. */
    public void refreshAsync() {
        statusLabel.setText("Refreshing...");
        new SwingWorker<DashboardStats, Void>() {
            @Override
            protected DashboardStats doInBackground() {
                return new DashboardService().getStats();
            }

            @Override
            protected void done() {
                try {
                    applyStats(get());
                    statusLabel.setText("Last updated: " + java.time.LocalTime.now()
                            .truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
                } catch (Exception e) {
                    statusLabel.setText("Dashboard refresh failed: " + e.getMessage());
                    UiHelper.showError(DashboardPanel.this,
                            "Could not load the dashboard: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void applyStats(DashboardStats stats) {
        studentsValue.setText(String.valueOf(stats.totalStudents()));
        coursesValue.setText(String.valueOf(stats.totalCourses()));
        enrollmentsValue.setText(String.valueOf(stats.totalEnrollments()));
        attendanceValue.setText(String.valueOf(stats.totalAttendanceRecords()));
        gradesValue.setText(String.valueOf(stats.totalGradeRecords()));
        avgAttendanceValue.setText(String.format(Locale.ROOT, "%.1f%%",
                stats.averageAttendancePercent()));
        lowAttendanceValue.setText(String.valueOf(stats.lowAttendanceCount()));
        gpaValue.setText(String.format(Locale.ROOT, "%.2f", stats.averageGpa()));

        lowAttendanceModel.setRowCount(0);
        for (var stat : new com.aat.tracker.service.AttendanceService().getLowAttendanceStudents()) {
            lowAttendanceModel.addRow(new Object[]{
                    stat.rollNumber(),
                    stat.studentName(),
                    stat.courseCode(),
                    String.format(Locale.ROOT, "%.1f%%", stat.attendancePercentage())
            });
        }
    }
}
