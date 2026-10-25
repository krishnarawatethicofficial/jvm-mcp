package dev.jvmmcp.core.model;

/**
 * Diagnostic assessment of JVM memory saturation, GC pause overhead, and actionable tuning advice.
 */
public record MemoryPressure(
        MemoryPressureLevel level,
        double heapUsageRatio,
        long totalGcTimeMs,
        long totalGcCount,
        String recommendation) {}
