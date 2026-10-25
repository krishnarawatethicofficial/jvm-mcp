package dev.jvmmcp.core.jmx;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jvmmcp.core.model.DeadlockReport;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeadlockDetectionTest {

    private JmxConnectionManager connectionManager;
    private ThreadMXBeanClient threadClient;
    private Thread worker1;
    private Thread worker2;

    @BeforeEach
    void setUp() {
        connectionManager = JmxConnectionManager.connectLocal();
        threadClient = new ThreadMXBeanClient(connectionManager.getMBeanServerConnection());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (worker1 != null) {
            worker1.interrupt();
        }
        if (worker2 != null) {
            worker2.interrupt();
        }
        if (worker1 != null) {
            worker1.join(1000);
        }
        if (worker2 != null) {
            worker2.join(1000);
        }
        if (connectionManager != null) {
            connectionManager.close();
        }
    }

    @Test
    @DisplayName(
            "detectDeadlocks should accurately identify circular lock chains and clean up cleanly")
    void shouldDetectCircularDeadlock() throws Exception {
        ReentrantLock lockA = new ReentrantLock();
        ReentrantLock lockB = new ReentrantLock();

        CountDownLatch thread1AcquiredLockA = new CountDownLatch(1);
        CountDownLatch thread2AcquiredLockB = new CountDownLatch(1);

        worker1 =
                new Thread(
                        () -> {
                            try {
                                lockA.lockInterruptibly();
                                try {
                                    thread1AcquiredLockA.countDown();
                                    thread2AcquiredLockB.await(5, TimeUnit.SECONDS);
                                    lockB.lockInterruptibly();
                                    try {
                                        // Deadlocked region
                                    } finally {
                                        lockB.unlock();
                                    }
                                } finally {
                                    lockA.unlock();
                                }
                            } catch (InterruptedException ignored) {
                                // Unblock cleanly on tearDown
                            }
                        },
                        "Deadlock-Worker-1");

        worker2 =
                new Thread(
                        () -> {
                            try {
                                lockB.lockInterruptibly();
                                try {
                                    thread2AcquiredLockB.countDown();
                                    thread1AcquiredLockA.await(5, TimeUnit.SECONDS);
                                    lockA.lockInterruptibly();
                                    try {
                                        // Deadlocked region
                                    } finally {
                                        lockA.unlock();
                                    }
                                } finally {
                                    lockB.unlock();
                                }
                            } catch (InterruptedException ignored) {
                                // Unblock cleanly on tearDown
                            }
                        },
                        "Deadlock-Worker-2");

        worker1.setDaemon(true);
        worker2.setDaemon(true);

        worker1.start();
        worker2.start();

        // Wait until both threads have started and acquired their initial locks
        boolean acquired =
                thread1AcquiredLockA.await(5, TimeUnit.SECONDS)
                        && thread2AcquiredLockB.await(5, TimeUnit.SECONDS);
        assertThat(acquired).isTrue();

        // Condition-based polling with Awaitility to eliminate timing flakiness in CI
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(50, TimeUnit.MILLISECONDS)
                .untilAsserted(
                        () -> {
                            DeadlockReport report = threadClient.detectDeadlocks();

                            assertThat(report).isNotNull();
                            assertThat(report.status()).isEqualTo("DETECTED");
                            assertThat(report.deadlockCount()).isGreaterThanOrEqualTo(2);
                            assertThat(report.chain()).hasSizeGreaterThanOrEqualTo(2);
                            assertThat(report.recommendation()).contains("Deadlock detected");

                            boolean containsThread1 =
                                    report.chain().stream()
                                            .anyMatch(
                                                    d ->
                                                            "Deadlock-Worker-1"
                                                                    .equals(d.threadName()));
                            boolean containsThread2 =
                                    report.chain().stream()
                                            .anyMatch(
                                                    d ->
                                                            "Deadlock-Worker-2"
                                                                    .equals(d.threadName()));

                            assertThat(containsThread1).isTrue();
                            assertThat(containsThread2).isTrue();
                        });
    }
}
