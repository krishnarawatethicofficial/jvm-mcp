package dev.jvmmcp.core.model;

/**
 * Entry in a live heap class histogram representing total instances and memory footprint of a
 * specific class.
 */
public record ClassHistogramItem(
        int rank, long instances, long bytes, double megabytes, String className) {
    public static ClassHistogramItem of(int rank, long instances, long bytes, String className) {
        double mb = bytes / (1024.0 * 1024.0);
        return new ClassHistogramItem(
                rank, instances, bytes, Math.round(mb * 100.0) / 100.0, className);
    }
}
