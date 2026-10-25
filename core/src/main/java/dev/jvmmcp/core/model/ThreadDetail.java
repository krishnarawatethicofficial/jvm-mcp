package dev.jvmmcp.core.model;

import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.List;

/** Detailed diagnostic model for an active JVM thread. */
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
        List<ThreadStackFrame> stackTrace) {
    public static ThreadDetail from(ThreadInfo info) {
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
                frames);
    }
}
