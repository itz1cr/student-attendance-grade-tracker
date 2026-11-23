package com.aat.tracker.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Generic statistical helpers (Week 8: generics and the Collections
 * framework). Works with any collection of {@link Number} values.
 */
public final class Statistics {

    private Statistics() {
    }

    /** Arithmetic mean of the values; 0 for an empty collection. */
    public static <T extends Number> double mean(Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (T value : values) {
            sum += value.doubleValue();
        }
        return sum / values.size();
    }

    /** Sum of the values; 0 for an empty collection. */
    public static <T extends Number> double sum(Collection<T> values) {
        if (values == null) {
            return 0.0;
        }
        double total = 0.0;
        for (T value : values) {
            total += value.doubleValue();
        }
        return total;
    }

    /** Median of the values; 0 for an empty collection. */
    public static <T extends Number> double median(List<T> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        List<Double> sorted = new ArrayList<>(values.size());
        for (T value : values) {
            sorted.add(value.doubleValue());
        }
        Collections.sort(sorted);
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 1) {
            return sorted.get(middle);
        }
        return (sorted.get(middle - 1) + sorted.get(middle)) / 2.0;
    }

    /** Percentage with two safe guards: total 0 -> 0, result clamped to 100. */
    public static double percentage(double obtained, double total) {
        if (total <= 0) {
            return 0.0;
        }
        return Math.min(100.0, obtained / total * 100.0);
    }
}
