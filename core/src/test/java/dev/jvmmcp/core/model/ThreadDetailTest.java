package dev.jvmmcp.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ThreadDetailTest {
    @Test
    void shouldReturnNullWhenThreadInfoIsNull() {
        assertThat(ThreadDetail.from(null)).isNull();
    }
}
