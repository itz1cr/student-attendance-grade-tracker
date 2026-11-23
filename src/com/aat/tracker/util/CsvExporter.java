package com.aat.tracker.util;

import com.aat.tracker.model.Reportable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Generic CSV report writer (Week 8: bounded generics, File I/O,
 * try-with-resources). One exporter instance can write any collection of
 * entities that implement the {@link Reportable} interface.
 *
 * @param <T> the entity type, bounded by the Reportable contract
 */
public class CsvExporter<T extends Reportable> {

    private final String header;

    public CsvExporter(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    /**
     * Writes the header plus one row per entity to {@code target},
     * creating missing parent directories.
     */
    public Path export(Path target, Collection<T> rows) throws IOException {
        Path absolute = target.toAbsolutePath();
        if (absolute.getParent() != null) {
            Files.createDirectories(absolute.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(absolute, StandardCharsets.UTF_8)) {
            writer.write(header);
            writer.newLine();
            for (T row : rows) {
                writer.write(toCsvLine(row.toCsvRow()));
                writer.newLine();
            }
        }
        return absolute;
    }

    /** Escapes a row of cells according to the CSV rules (RFC 4180). */
    public static String toCsvLine(String[] cells) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            String cell = cells[i] == null ? "" : cells[i];
            if (cell.contains(",") || cell.contains("\"") || cell.contains("\n")
                    || cell.contains("\r")) {
                line.append('"').append(cell.replace("\"", "\"\"")).append('"');
            } else {
                line.append(cell);
            }
        }
        return line.toString();
    }
}
