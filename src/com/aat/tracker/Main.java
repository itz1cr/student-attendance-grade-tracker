package com.aat.tracker;

import com.aat.tracker.db.DatabaseManager;
import com.aat.tracker.db.SeedDemoData;
import com.aat.tracker.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application entry point.
 *
 * <p>1. Applies the system look-and-feel.<br>
 * 2. Creates the SQLite database and schema (Week 7).<br>
 * 3. Seeds demo data on the very first run.<br>
 * 4. Opens the Swing GUI on the Event Dispatch Thread (Week 6).</p>
 */
public final class Main {

    public static final String APP_NAME = "Student Attendance & Grade Tracker";
    public static final String APP_VERSION = "1.0";

    private Main() {
    }

    public static void main(String[] args) {
        applyLookAndFeel();
        DatabaseManager.getInstance().initDatabase();
        if (!"false".equalsIgnoreCase(System.getProperty("tracker.seed", "true"))) {
            SeedDemoData.seedIfEmpty();
        }
        // Swing rule: build and show the GUI on the EDT, never on main().
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    private static void applyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // A fallback look-and-feel is always available; ignore failures.
        }
    }
}
