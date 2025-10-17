package arn.roub.krabot.memory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

/**
 * Service for monitoring memory usage and handling OutOfMemoryError conditions.
 * Registers listeners for memory threshold notifications and provides utilities
 * for memory status checking.
 */
@ApplicationScoped
public class MemoryMonitoringService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryMonitoringService.class);
    private static final double BYTES_TO_MB = 1024.0 * 1024.0;
    
    private final int warningThreshold;
    private final int criticalThreshold;
    private final MemoryMXBean memoryMXBean;
    
    public MemoryMonitoringService(
            @ConfigProperty(name = "memory.warning.threshold.percent", defaultValue = "80") int warningThreshold,
            @ConfigProperty(name = "memory.critical.threshold.percent", defaultValue = "90") int criticalThreshold) {
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
    }

    @PostConstruct
    void initialize() {
        setupMemoryThresholdNotifications();
        setupOutOfMemoryErrorHandler();
        logInitialMemoryState();
    }

    /**
     * Sets up JMX notifications for memory threshold breaches.
     */
    private void setupMemoryThresholdNotifications() {
        try {
            // Get the platform MBean server and register listener
            NotificationEmitter emitter = (NotificationEmitter) memoryMXBean;
            
            NotificationListener listener = (Notification notification, Object handback) -> {
                if (notification.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                    CompositeData cd = (CompositeData) notification.getUserData();
                    MemoryNotificationInfo info = MemoryNotificationInfo.from(cd);
                    
                    long usedMB = info.getUsage().getUsed() / (long)BYTES_TO_MB;
                    long maxMB = info.getUsage().getMax() / (long)BYTES_TO_MB;
                    int usagePercent = (int)((usedMB * 100) / maxMB);
                    
                    LOGGER.error("Memory threshold exceeded! Pool: {}, Used: {} MB / {} MB ({}%)",
                            info.getPoolName(), usedMB, maxMB, usagePercent);
                }
            };
            
            emitter.addNotificationListener(listener, null, null);
            
            // Set thresholds for heap memory pools
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
                    long maxMemory = pool.getUsage().getMax();
                    if (maxMemory > 0) {
                        // Set threshold at critical level
                        long threshold = (maxMemory * criticalThreshold) / 100;
                        pool.setUsageThreshold(threshold);
                        LOGGER.debug("Set memory threshold for pool '{}' at {} MB ({}%)", 
                                pool.getName(), threshold / (long)BYTES_TO_MB, criticalThreshold);
                    }
                }
            }
            
            LOGGER.info("Memory threshold notifications configured successfully");
        } catch (Exception e) {
            LOGGER.warn("Failed to setup memory threshold notifications: {}", e.getMessage());
        }
    }

    /**
     * Sets up a shutdown hook to handle OutOfMemoryError gracefully.
     */
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

    /**
     * Handles OutOfMemoryError by logging detailed memory information and attempting cleanup.
     */
    private void handleOutOfMemoryError(OutOfMemoryError error, Thread thread) {
        try {
            LOGGER.error("========================================");
            LOGGER.error("OUT OF MEMORY ERROR DETECTED!");
            LOGGER.error("========================================");
            LOGGER.error("Thread: {}", thread.getName());
            LOGGER.error("Error message: {}", error.getMessage());
            
            // Log current memory state
            Runtime runtime = Runtime.getRuntime();
            long maxMemoryMB = runtime.maxMemory() / (long)BYTES_TO_MB;
            long totalMemoryMB = runtime.totalMemory() / (long)BYTES_TO_MB;
            long freeMemoryMB = runtime.freeMemory() / (long)BYTES_TO_MB;
            long usedMemoryMB = totalMemoryMB - freeMemoryMB;
            
            LOGGER.error("Memory state at OOM:");
            LOGGER.error("  Max memory:   {} MB", maxMemoryMB);
            LOGGER.error("  Total memory: {} MB", totalMemoryMB);
            LOGGER.error("  Used memory:  {} MB", usedMemoryMB);
            LOGGER.error("  Free memory:  {} MB", freeMemoryMB);
            
            // Log memory pool details
            LOGGER.error("Memory pool details:");
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                long poolUsedMB = pool.getUsage().getUsed() / (long)BYTES_TO_MB;
                long poolMaxMB = pool.getUsage().getMax() > 0 ? 
                        pool.getUsage().getMax() / (long)BYTES_TO_MB : -1;
                LOGGER.error("  {}: {} MB / {} MB", pool.getName(), poolUsedMB, 
                        poolMaxMB > 0 ? poolMaxMB + " MB" : "unlimited");
            }
            
            LOGGER.error("========================================");
            
            // Attempt emergency garbage collection
            LOGGER.error("Attempting emergency garbage collection...");
            System.gc();
            
            long usedAfterGC = (runtime.totalMemory() - runtime.freeMemory()) / (long)BYTES_TO_MB;
            LOGGER.error("Memory after emergency GC: {} MB", usedAfterGC);
            
        } catch (Throwable t) {
            // Even logging failed - print to stderr
            System.err.println("FATAL: OutOfMemoryError and logging also failed: " + t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * Logs the initial memory configuration on startup.
     */
    private void logInitialMemoryState() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemoryMB = runtime.maxMemory() / (long)BYTES_TO_MB;
        long totalMemoryMB = runtime.totalMemory() / (long)BYTES_TO_MB;
        long freeMemoryMB = runtime.freeMemory() / (long)BYTES_TO_MB;
        
        LOGGER.info("Initial memory configuration:");
        LOGGER.info("  Max heap size: {} MB", maxMemoryMB);
        LOGGER.info("  Initial heap size: {} MB", totalMemoryMB);
        LOGGER.info("  Free memory: {} MB", freeMemoryMB);
        LOGGER.info("  Warning threshold: {}%", warningThreshold);
        LOGGER.info("  Critical threshold: {}%", criticalThreshold);
        
        // Log GC information
        LOGGER.info("Garbage Collector info:");
        ManagementFactory.getGarbageCollectorMXBeans().forEach(gc -> 
            LOGGER.info("  {}: {} collections, {} ms total time", 
                    gc.getName(), gc.getCollectionCount(), gc.getCollectionTime())
        );
    }

    /**
     * Gets the current memory usage percentage.
     */
    public int getCurrentMemoryUsagePercent() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return (int)((usedMemory * 100) / maxMemory);
    }

    /**
     * Checks if memory usage is above warning threshold.
     */
    public boolean isMemoryAboveWarningThreshold() {
        return getCurrentMemoryUsagePercent() >= warningThreshold;
    }

    /**
     * Checks if memory usage is above critical threshold.
     */
    public boolean isMemoryAboveCriticalThreshold() {
        return getCurrentMemoryUsagePercent() >= criticalThreshold;
    }
}
