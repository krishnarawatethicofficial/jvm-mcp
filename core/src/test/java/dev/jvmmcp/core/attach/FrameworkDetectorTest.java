package dev.jvmmcp.core.attach;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jvmmcp.core.model.Framework;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FrameworkDetectorTest {

    @ParameterizedTest(name = "Detect {1} for displayName=''{0}''")
    @CsvSource({
        "org.springframework.boot.loader.JarLauncher, SPRING_BOOT",
        "com.example.DemoApplication (SpringBoot), SPRING_BOOT",
        "io.quarkus.runner.GeneratedMain, QUARKUS",
        "io.micronaut.runtime.Micronaut, MICRONAUT",
        "com.example.plain.MainApp, PLAIN_JAVA"
    })
    @DisplayName("Should detect expected framework based on process display name")
    void shouldDetectExpectedFramework(String displayName, Framework expected) {
        Framework result = FrameworkDetector.detect(displayName, "");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return UNKNOWN for null or empty metadata")
    void shouldReturnUnknownForBlankMetadata() {
        assertThat(FrameworkDetector.detect(null, null)).isEqualTo(Framework.UNKNOWN);
        assertThat(FrameworkDetector.detect("", "")).isEqualTo(Framework.UNKNOWN);
    }
}
