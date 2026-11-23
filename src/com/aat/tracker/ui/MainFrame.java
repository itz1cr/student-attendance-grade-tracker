package com.aat.tracker.ui;

import com.aat.tracker.Main;
import com.aat.tracker.db.DatabaseManager;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Main application window (Week 6: navigation between screens via a
 * {@link JTabbedPane}; Week 5: inheritance from JFrame).
 *
 * <p>Tabs: Dashboard, Students, Courses, Attendance, Grades, Reports.
 * Switching tabs refreshes the dashboard so figures stay current.</p>
 */
public class MainFrame extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();
    private final DashboardPanel dashboardPanel = new DashboardPanel();

    public MainFrame() {
        super(Main.APP_NAME + " v" + Main.APP_VERSION);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 740);
        setLocationRelativeTo(null); // centre on screen

        setJMenuBar(buildMenuBar());

        tabs.addTab("Dashboard", dashboardPanel);
        tabs.addTab("Students", new StudentPanel());
        tabs.addTab("Courses", new CoursePanel());
        tabs.addTab("Attendance", new AttendancePanel());
        tabs.addTab("Grades", new GradePanel());
        tabs.addTab("Reports", new ReportPanel());

        // Refresh the dashboard whenever the user navigates to another tab.
        tabs.addChangeListener(event -> dashboardPanel.refreshAsync());

        add(tabs, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    /** Accessor used by the screenshot generator. */
    public JTabbedPane getTabs() {
        return tabs;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem refreshItem = new JMenuItem("Refresh Dashboard");
        refreshItem.addActionListener(event -> dashboardPanel.refreshAsync());
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(event -> dispose());
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(event -> JOptionPane.showMessageDialog(this,
                Main.APP_NAME + " v" + Main.APP_VERSION
                        + "\n\nJava AAT project - Swing GUI + JDBC (SQLite)."
                        + "\nTrack student attendance, grades, GPA and export CSV reports."
                        + "\n\nOOP: inheritance, polymorphism, abstract classes, interfaces,"
                        + "\n     overloading/overriding, encapsulation, static & final."
                        + "\nAdvanced: collections, generics, enums, file I/O,"
                        + "\n     user-defined & built-in exception handling.",
                "About " + Main.APP_NAME, JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        JLabel databaseLabel = new JLabel("Database: " + DatabaseManager.getInstance().getJdbcUrl());
        databaseLabel.setFont(databaseLabel.getFont().deriveFont(Font.PLAIN, 11));
        statusBar.add(databaseLabel, BorderLayout.WEST);
        return statusBar;
    }
}
