package dev.jvmmcp.core.port;

import dev.jvmmcp.core.model.HeapSummary;

public interface MemoryDiagnosticPort extends AutoCloseable {
    HeapSummary getHeapSummary(long pid) throws Exception;

    @Override
    void close() throws Exception;
}
