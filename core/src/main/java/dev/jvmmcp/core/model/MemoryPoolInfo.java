package dev.jvmmcp.core.model;

/** Diagnostic metrics for a specific JVM Memory Pool (Eden, Survivor, Tenured, Metaspace, etc.). */
public record MemoryPoolInfo(
        String name, String type, MemoryUsageInfo usage, MemoryUsageInfo peakUsage) {}
