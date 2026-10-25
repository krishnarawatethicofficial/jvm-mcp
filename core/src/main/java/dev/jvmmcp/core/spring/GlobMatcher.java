package dev.jvmmcp.core.spring;

import java.util.regex.Pattern;

/** Utility for case-insensitive glob pattern matching (*, ?). */
public final class GlobMatcher {

    private GlobMatcher() {}

    public static boolean matches(String text, String globPattern) {
        if (text == null) {
            return false;
        }
        if (globPattern == null || globPattern.isBlank() || "*".equals(globPattern)) {
            return true;
        }
        if (globPattern.length() > 256) {
            return false;
        }
        Pattern pattern = compileGlob(globPattern);
        return pattern.matcher(text).matches();
    }

    public static Pattern compileGlob(String globPattern) {
        if (globPattern == null) {
            return Pattern.compile(".*");
        }
        if (globPattern.length() > 256) {
            throw new IllegalArgumentException(
                    "Glob pattern exceeds maximum length of 256 characters");
        }
        StringBuilder regex = new StringBuilder("^");
        boolean lastWasStar = false;
        for (int i = 0; i < globPattern.length(); i++) {
            char c = globPattern.charAt(i);
            if (c == '*') {
                if (!lastWasStar) {
                    regex.append(".*");
                    lastWasStar = true;
                }
            } else {
                lastWasStar = false;
                switch (c) {
                    case '?' -> regex.append(".");
                    case '.', '(', ')', '[', ']', '{', '}', '^', '$', '+', '|', '\\' -> {
                        regex.append('\\').append(c);
                    }
                    default -> regex.append(c);
                }
            }
        }
        regex.append("$");
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
    }
}
