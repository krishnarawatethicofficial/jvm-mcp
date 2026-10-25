package dev.jvmmcp.core.model;

import java.util.List;

/** Top N classes ranked by instance count and heap memory footprint. */
public record HeapHistogram(
        long pid,
        long totalInstances,
        long totalBytes,
        double totalMegabytes,
        List<ClassHistogramItem> topClasses) {}
