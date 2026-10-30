package dev.jvmmcp.core.model;

import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.List;

/**
 * Detailed diagnostic model for an active JVM thread.
 *
 * @param cpuTimeNs cumulative CPU time consumed by the thread, or {@code null} when unavailable
 * @param userTimeNs cumulative user-mode CPU time consumed by the thread, or {@code null} when
 *     unavailable
 */
public record ThreadDetail(
        long threadId,
        String threadName,
        String state,
        String lockName,
        Long lockOwnerId,
        String lockOwnerName,
        long blockedTimeMs,
        long blockedCount,
        long waitedTimeMs,
        long waitedCount,
        boolean inNative,
        boolean suspended,
        List<ThreadStackFrame> stackTrace,
        Long cpuTimeNs,
        Long userTimeNs) {

    /** Backward-compatible constructor for callers that do not have CPU-time measurements. */
    public ThreadDetail(
            long threadId,
            String threadName,
            String state,
            String lockName,
            Long lockOwnerId,
            String lockOwnerName,
            long blockedTimeMs,
            long blockedCount,
            long waitedTimeMs,
            long waitedCount,
            boolean inNative,
            boolean suspended,
            List<ThreadStackFrame> stackTrace) {
        this(
                threadId,
                threadName,
                state,
                lockName,
                lockOwnerId,
                lockOwnerName,
                blockedTimeMs,
                blockedCount,
                waitedTimeMs,
                waitedCount,
                inNative,
                suspended,
                stackTrace,
                null,
                null);
    }

    public static ThreadDetail from(ThreadInfo info) {
        return from(info, null, null);
    }

    /**
     * Creates a thread detail with CPU measurements, expressed in nanoseconds. A {@code null}
     * measurement means the target JVM did not make it available.
     */
    public static ThreadDetail from(ThreadInfo info, Long cpuTimeNs, Long userTimeNs) {
        if (info == null) {
            return null;
        }

        List<ThreadStackFrame> frames =
                Arrays.stream(info.getStackTrace()).map(ThreadStackFrame::from).toList();

        Long ownerId = info.getLockOwnerId() >= 0 ? info.getLockOwnerId() : null;

        return new ThreadDetail(
                info.getThreadId(),
                info.getThreadName(),
                info.getThreadState().name(),
                info.getLockName(),
                ownerId,
                info.getLockOwnerName(),
                info.getBlockedTime(),
                info.getBlockedCount(),
                info.getWaitedTime(),
                info.getWaitedCount(),
                info.isInNative(),
                info.isSuspended(),
                frames,
                cpuTimeNs,
                userTimeNs);
    }
}
