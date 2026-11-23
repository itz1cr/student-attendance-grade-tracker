package com.aat.tracker.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;

/**
 * Shared Swing helpers used by every screen (Week 6: GUI building blocks,
 * user feedback via dialogs).
 */
public final class UiHelper {

    public static final int DEFAULT_PADDING = 10;

    /** Title font used for panel headers (Week 4: static final). */
    public static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);

    private UiHelper() {
    }

    /** Error dialog for validation failures and data-access problems. */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /** Informational dialog for successful operations (user feedback). */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Yes/No confirmation used before destructive actions. */
    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION;
    }

    public static JButton button(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    public static JTextField textField(int columns) {
        return new JTextField(columns);
    }

    /** Panel with a titled border, used for grouped forms. */
    public static JPanel titledPanel(String title, JComponent content) {
        JPanel panel = new JPanel();
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
        panel.setBorder(border);
        panel.add(content);
        return panel;
    }

    public static JLabel titleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        return label;
    }

    /** Distributes the table columns according to the given weight fractions. */
    public static void setColumnWidths(JTable table, double[] fractions) {
        int totalWidth = table.getPreferredScrollableViewportSize().width;
        for (int i = 0; i < fractions.length && i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth((int) (totalWidth * fractions[i]));
        }
    }
}
