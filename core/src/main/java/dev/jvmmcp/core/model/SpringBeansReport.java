package dev.jvmmcp.core.model;

import java.util.List;

/** Full Spring Beans inspection report across all active application contexts. */
public record SpringBeansReport(
        long pid, String discoverySource, int totalBeans, List<SpringContextBeans> contexts) {
    public List<SpringBeanDetail> getAllBeans() {
        return contexts.stream().flatMap(c -> c.beans().stream()).toList();
    }
}
