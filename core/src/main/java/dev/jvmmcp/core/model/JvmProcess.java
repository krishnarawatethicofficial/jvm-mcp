package dev.jvmmcp.core.model;

/** Metadata representing a discovered Java Virtual Machine process on the host. */
public record JvmProcess(
        long pid,
        String displayName,
        String mainClass,
        String javaVersion,
        Framework framework,
        boolean isAttachable) {}
