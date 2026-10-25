package dev.jvmmcp.core.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GlobMatcherTest {

    @ParameterizedTest(name = "Glob ''{1}'' matches ''{0}'' -> {2}")
    @CsvSource({
        "com.example.OrderService, *Service*, true",
        "orderService, *service*, true",
        "PaymentController, *Controller, true",
        "PaymentController, *Service, false",
        "userRepository, User*, true",
        "authManager, *auth*, true",
        "com.example.repo.UserRepo, com.example.*, true",
        "org.springframework.boot.Service, *spring*, true",
        "anything, *, true",
        "anything, '', true",
        "anything, '   ', true",
        "ServiceA, Service?, true",
        "ServiceAB, Service?, false",
        "com.example(v1)[core], com.example(*)[*], true",
        "test+item$price^val{1}|2\\3, *+item$price^*, true"
    })
    @DisplayName("GlobMatcher should match patterns case-insensitively with glob wildcards")
    void shouldMatchGlobPatterns(String text, String pattern, boolean expected) {
        assertThat(GlobMatcher.matches(text, pattern)).isEqualTo(expected);
    }

    @Test
    @DisplayName("matches returns false when text is null")
    void shouldReturnFalseForNullText() {
        assertThat(GlobMatcher.matches(null, "somePattern")).isFalse();
        assertThat(GlobMatcher.matches(null, null)).isFalse();
        assertThat(GlobMatcher.matches(null, "*")).isFalse();
    }

    @Test
    @DisplayName("Consecutive wildcards should collapse and match correctly without ReDoS")
    void shouldCollapseConsecutiveWildcards() {
        assertThat(GlobMatcher.matches("foobar", "foo*****bar")).isTrue();
        assertThat(GlobMatcher.matches("fooXYZbar", "foo***bar")).isTrue();
        assertThat(GlobMatcher.matches("foobaz", "foo***bar")).isFalse();
    }

    @Test
    @DisplayName("Excessive glob pattern length throws IllegalArgumentException in compileGlob")
    void shouldRejectExcessiveGlobLength() {
        String excessivePattern = "*".repeat(300);
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> GlobMatcher.compileGlob(excessivePattern));
        assertThat(GlobMatcher.matches("test", excessivePattern)).isFalse();
    }
}
