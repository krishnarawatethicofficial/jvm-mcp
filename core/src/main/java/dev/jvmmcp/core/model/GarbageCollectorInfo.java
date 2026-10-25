package dev.jvmmcp.core.model;

/** Diagnostic metrics for a specific JVM Garbage Collector (G1 Young/Old, ZGC, Parallel, etc.). */
public record GarbageCollectorInfo(
        String name, long collectionCount, long collectionTimeMs, String[] memoryPoolNames) {}
