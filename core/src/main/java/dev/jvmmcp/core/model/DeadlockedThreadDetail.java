package dev.jvmmcp.core.model;

/** Detailed representation of a single thread involved in a circular deadlock condition. */
public record DeadlockedThreadDetail(
        long threadId,
        String threadName,
        String state,
        String waitingToAcquire,
        Long lockOwnerId,
        String lockOwnerName,
        String stackTop) {}
