package dev.jvmmcp.server.transport;

import java.io.IOException;

/** Thrown when a line in an input stream exceeds the configured maximum byte limit. */
public class LineTooLargeException extends IOException {

    private final int maxBytes;

    public LineTooLargeException(String message, int maxBytes) {
        super(message);
        this.maxBytes = maxBytes;
    }

    public int getMaxBytes() {
        return maxBytes;
    }
}
