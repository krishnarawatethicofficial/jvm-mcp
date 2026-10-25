package dev.jvmmcp.core.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import net.jqwik.api.*;

class GlobMatcherPropertyTest {

    @Property
    void wildcardsAlwaysMatchAnyString(@ForAll String text) {
        assertThat(GlobMatcher.matches(text, "*")).isTrue();
        assertThat(GlobMatcher.matches(text, "")).isTrue();
        assertThat(GlobMatcher.matches(text, null)).isTrue();
    }

    @Property
    void exactStringAlwaysMatchesItself(@ForAll String text) {
        if (text != null) {
            assertThat(GlobMatcher.matches(text, text)).isTrue();
        }
    }

    @Property
    void patternWithPrefixWildcardMatchesSuffixedString(
            @ForAll("alphanumeric") String prefix, @ForAll("alphanumeric") String suffix) {
        String text = prefix + suffix;
        String pattern = "*" + suffix;
        assertThat(GlobMatcher.matches(text, pattern)).isTrue();
    }

    @Property
    void patternWithSuffixWildcardMatchesPrefixedString(
            @ForAll("alphanumeric") String prefix, @ForAll("alphanumeric") String suffix) {
        String text = prefix + suffix;
        String pattern = prefix + "*";
        assertThat(GlobMatcher.matches(text, pattern)).isTrue();
    }

    @Property
    void arbitraryRandomPatternsNeverThrowExceptions(@ForAll String text, @ForAll String pattern) {
        assertThatCode(() -> GlobMatcher.matches(text, pattern)).doesNotThrowAnyException();
    }

    @Provide
    Arbitrary<String> alphanumeric() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(20);
    }
}
