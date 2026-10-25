package dev.jvmmcp.core.model;

import java.util.List;

/** Diagnostic metadata for a single Spring bean registered in the ApplicationContext. */
public record SpringBeanDetail(
        String name,
        List<String> aliases,
        String scope,
        String type,
        String resource,
        List<String> dependencies) {}
