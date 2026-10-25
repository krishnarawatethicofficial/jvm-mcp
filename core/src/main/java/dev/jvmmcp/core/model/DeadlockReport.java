package dev.jvmmcp.core.model;

import java.util.List;

/** Structured diagnostic deadlock report consumed directly by LLM assistants. */
public record DeadlockReport(
        String status,
        int deadlockCount,
        List<DeadlockedThreadDetail> chain,
        String recommendation) {
    public static DeadlockReport none() {
        return new DeadlockReport(
                "NONE",
                0,
                List.of(),
                "No deadlocks detected. All monitored threads are operating normally.");
    }

    public static DeadlockReport detected(
            List<DeadlockedThreadDetail> chain, String recommendation) {
        return new DeadlockReport("DETECTED", chain.size(), chain, recommendation);
    }
}
