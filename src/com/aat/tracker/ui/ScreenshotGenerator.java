package com.aat.tracker.ui;

import com.aat.tracker.db.DatabaseManager;
import com.aat.tracker.db.SeedDemoData;

import javax.imageio.ImageIO;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the screenshots used in the project report (Week 10:
 * documentation). The frame is realised as a real window placed far
 * off-screen, so Swing lays out and paints every component correctly while
 * nothing flashes on the desktop.
 */
public final class ScreenshotGenerator {

    private static final String[] TAB_NAMES = {
            "dashboard", "students", "courses", "attendance", "grades", "reports"
    };

    private ScreenshotGenerator() {
    }

    public static void main(String[] args) throws Exception {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // fall back to the default look-and-feel
        }

        DatabaseManager.getInstance().initDatabase();
        SeedDemoData.seedIfEmpty();

        Path outDir = Path.of("docs", "screenshots");
        Files.createDirectories(outDir);

        final MainFrame[] holder = new MainFrame[1];
        final List<String> written = new ArrayList<>();

        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame();
            frame.setSize(1180, 740);
            frame.setLocation(-2400, -1800); // off-screen: realised but invisible
            frame.setVisible(true);
            holder[0] = frame;
        });
        Thread.sleep(1000); // let the initial paint and data loading settle

        JTabbedPane tabs = holder[0].getTabs();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            final int index = i;
            SwingUtilities.invokeAndWait(() -> tabs.setSelectedIndex(index));
            Thread.sleep(450); // let lazy loading / async refresh finish

            final String name = i < TAB_NAMES.length ? TAB_NAMES[i] : "tab" + i;
            final Path file = outDir.resolve(name + ".png");
            SwingUtilities.invokeAndWait(() -> {
                BufferedImage image = new BufferedImage(
                        holder[0].getWidth(), holder[0].getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                holder[0].printAll(graphics);
                graphics.dispose();
                try {
                    ImageIO.write(image, "png", file.toFile());
                    written.add(file.toString());
                } catch (Exception e) {
                    System.err.println("Could not write " + file + ": " + e.getMessage());
                }
            });
        }

        SwingUtilities.invokeAndWait(holder[0]::dispose);

        System.out.println("Screenshots written to " + outDir.toAbsolutePath());
        for (String file : written) {
            System.out.println("  - " + file);
        }
    }
}
