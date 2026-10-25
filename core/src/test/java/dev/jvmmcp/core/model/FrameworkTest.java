package dev.jvmmcp.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FrameworkTest {
    @Test
    void shouldReturnCorrectDisplayNames() {
        assertThat(Framework.SPRING_BOOT.getDisplayName()).isEqualTo("Spring Boot");
        assertThat(Framework.valueOf("QUARKUS")).isEqualTo(Framework.QUARKUS);
        assertThat(Framework.values())
                .contains(Framework.MICRONAUT, Framework.PLAIN_JAVA, Framework.UNKNOWN);
    }
}
