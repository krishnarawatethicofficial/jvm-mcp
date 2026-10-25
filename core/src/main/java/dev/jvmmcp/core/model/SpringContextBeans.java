package dev.jvmmcp.core.model;

import java.util.List;

/** Collection of beans grouped within a specific Spring ApplicationContext hierarchy. */
public record SpringContextBeans(
        String contextId, String parentId, int beanCount, List<SpringBeanDetail> beans) {}
