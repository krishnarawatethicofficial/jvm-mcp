package dev.jvmmcp.core.model;

import java.lang.management.MemoryUsage;

/**
 * Immutable memory usage snapshot formatted for programmatic and LLM consumption.
 *
 * <p>When JMX reports no upper bound (a negative {@code max}, e.g. non-heap memory or an unbounded
 * pool such as Metaspace), {@code maxMb} and {@code usedPercent} carry the {@code -1.0} sentinel
 * instead of passing {@code committedBytes} off as the quota. A {@code max} of exactly zero is a
 * real bound and keeps numeric values.
 */
public record MemoryUsageInfo(
        long initBytes,
        long usedBytes,
        long committedBytes,
        long maxBytes,
        double usedPercent,
        double usedMb,
        double maxMb) {
    /** Reported for {@code maxMb} / {@code usedPercent} when the JVM exposes no upper bound. */
    private static final double UNKNOWN_SENTINEL = -1.0;

    public static MemoryUsageInfo from(MemoryUsage usage) {
        if (usage == null) {
            return new MemoryUsageInfo(0, 0, 0, 0, 0.0, 0.0, 0.0);
        }
        long init = usage.getInit();
        long used = usage.getUsed();
        long committed = usage.getCommitted();
        long max = usage.getMax();
        double usedMb = used / (1024.0 * 1024.0);
        double usedPercent;
        double maxMb;
        if (max < 0) {
            // Unbounded: committed is the current footprint, never a quota.
            usedPercent = UNKNOWN_SENTINEL;
            maxMb = UNKNOWN_SENTINEL;
        } else if (max > 0) {
            usedPercent = ((double) used / (double) max) * 100.0;
            maxMb = max / (1024.0 * 1024.0);
        } else {
            usedPercent = committed > 0 ? ((double) used / (double) committed) * 100.0 : 0.0;
            maxMb = committed / (1024.0 * 1024.0);
        }

        return new MemoryUsageInfo(
                init,
                used,
                committed,
                max,
                Math.round(usedPercent * 100.0) / 100.0,
                Math.round(usedMb * 100.0) / 100.0,
                Math.round(maxMb * 100.0) / 100.0);
    }
}
