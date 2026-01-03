package arn.roub.krabot.shared.memory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;

/**
 * Service cross-cutting pour le monitoring de la mémoire.
 */
@ApplicationScoped
public class MemoryMonitoringService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryMonitoringService.class);
    private static final double BYTES_TO_MB = 1024.0 * 1024.0;

    private final int warningThreshold;
    private final int criticalThreshold;
    private final MemoryMXBean memoryMXBean;
    private final boolean isNativeMode;

    public MemoryMonitoringService(
            @ConfigProperty(name = "memory.warning.threshold.percent", defaultValue = "80") int warningThreshold,
            @ConfigProperty(name = "memory.critical.threshold.percent", defaultValue = "90") int criticalThreshold
    ) {
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.isNativeMode = System.getProperty("org.graalvm.nativeimage.imagecode") != null;
    }

    @PostConstruct
    void initialize() {
        setupMemoryThresholdNotifications();
        setupOutOfMemoryErrorHandler();
        logInitialMemoryState();
    }

    private void setupMemoryThresholdNotifications() {
        if (isNativeMode) {
            LOGGER.info("Running in native mode - JMX memory threshold notifications not available");
            return;
        }

        try {
            NotificationEmitter emitter = (NotificationEmitter) memoryMXBean;

            NotificationListener listener = (Notification notification, Object handback) -> {
                if (notification.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                    CompositeData cd = (CompositeData) notification.getUserData();
                    MemoryNotificationInfo info = MemoryNotificationInfo.from(cd);

                    long usedMB = info.getUsage().getUsed() / (long) BYTES_TO_MB;
                    long maxMB = info.getUsage().getMax() / (long) BYTES_TO_MB;
                    int usagePercent = (int) ((usedMB * 100) / maxMB);

                    LOGGER.error("Memory threshold exceeded! Pool: {}, Used: {} MB / {} MB ({}%)",
                            info.getPoolName(), usedMB, maxMB, usagePercent);
                }
            };

            emitter.addNotificationListener(listener, null, null);

            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
                    long maxMemory = pool.getUsage().getMax();
                    if (maxMemory > 0) {
                        long threshold = (maxMemory * criticalThreshold) / 100;
                        pool.setUsageThreshold(threshold);
                        LOGGER.debug("Set memory threshold for pool '{}' at {} MB ({}%)",
                                pool.getName(), threshold / (long) BYTES_TO_MB, criticalThreshold);
                    }
                }
            }

            LOGGER.info("Memory threshold notifications configured successfully");
        } catch (Exception e) {
            LOGGER.warn("Failed to setup memory threshold notifications: {}", e.getMessage());
        }
    }

    private void setupOutOfMemoryErrorHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof OutOfMemoryError) {
                handleOutOfMemoryError((OutOfMemoryError) throwable, thread);
            } else {
                LOGGER.error("Uncaught exception in thread {}: {}",
                        thread.getName(), throwable.getMessage(), throwable);
            }
        });

        LOGGER.info("OutOfMemoryError handler configured");
    }

    private void handleOutOfMemoryError(OutOfMemoryError error, Thread thread) {
        try {
            LOGGER.error("========================================");
            LOGGER.error("OUT OF MEMORY ERROR DETECTED!");
            LOGGER.error("========================================");
            LOGGER.error("Thread: {}", thread.getName());
            LOGGER.error("Error message: {}", error.getMessage());

            Runtime runtime = Runtime.getRuntime();
            long maxMemoryMB = runtime.maxMemory() / (long) BYTES_TO_MB;
            long totalMemoryMB = runtime.totalMemory() / (long) BYTES_TO_MB;
            long freeMemoryMB = runtime.freeMemory() / (long) BYTES_TO_MB;
            long usedMemoryMB = totalMemoryMB - freeMemoryMB;

            LOGGER.error("Memory state at OOM:");
            LOGGER.error("  Max memory:   {} MB", maxMemoryMB);
            LOGGER.error("  Total memory: {} MB", totalMemoryMB);
            LOGGER.error("  Used memory:  {} MB", usedMemoryMB);
            LOGGER.error("  Free memory:  {} MB", freeMemoryMB);

            if (!isNativeMode) {
                LOGGER.error("Memory pool details:");
                for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                    long poolUsedMB = pool.getUsage().getUsed() / (long) BYTES_TO_MB;
                    long poolMaxMB = pool.getUsage().getMax() > 0 ?
                            pool.getUsage().getMax() / (long) BYTES_TO_MB : -1;
                    LOGGER.error("  {}: {} MB / {} MB", pool.getName(), poolUsedMB,
                            poolMaxMB > 0 ? poolMaxMB + " MB" : "unlimited");
                }
            }

            LOGGER.error("========================================");
            LOGGER.error("Attempting emergency garbage collection...");
            System.gc();

            long usedAfterGC = (runtime.totalMemory() - runtime.freeMemory()) / (long) BYTES_TO_MB;
            LOGGER.error("Memory after emergency GC: {} MB", usedAfterGC);

        } catch (Throwable t) {
            System.err.println("FATAL: OutOfMemoryError and logging also failed: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void logInitialMemoryState() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemoryMB = runtime.maxMemory() / (long) BYTES_TO_MB;
        long totalMemoryMB = runtime.totalMemory() / (long) BYTES_TO_MB;
        long freeMemoryMB = runtime.freeMemory() / (long) BYTES_TO_MB;

        LOGGER.info("Initial memory configuration:");
        LOGGER.info("  Max heap size: {} MB", maxMemoryMB);
        LOGGER.info("  Initial heap size: {} MB", totalMemoryMB);
        LOGGER.info("  Free memory: {} MB", freeMemoryMB);
        LOGGER.info("  Warning threshold: {}%", warningThreshold);
        LOGGER.info("  Critical threshold: {}%", criticalThreshold);

        if (!isNativeMode) {
            LOGGER.info("Garbage Collector info:");
            ManagementFactory.getGarbageCollectorMXBeans().forEach(gc ->
                    LOGGER.info("  {}: {} collections, {} ms total time",
                            gc.getName(), gc.getCollectionCount(), gc.getCollectionTime())
            );
        } else {
            LOGGER.info("Running in native mode - detailed GC info not available");
        }
    }

    public int getCurrentMemoryUsagePercent() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return (int) ((usedMemory * 100) / maxMemory);
    }

    public boolean isMemoryAboveWarningThreshold() {
        return getCurrentMemoryUsagePercent() >= warningThreshold;
    }

    public boolean isMemoryAboveCriticalThreshold() {
        return getCurrentMemoryUsagePercent() >= criticalThreshold;
    }
}
