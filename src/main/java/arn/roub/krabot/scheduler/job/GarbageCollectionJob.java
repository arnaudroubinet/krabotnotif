package arn.roub.krabot.scheduler.job;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduled job that performs explicit garbage collection to help prevent memory leaks
 * and ensure optimal memory usage in Kubernetes environments.
 * 
 * This job runs hourly by default (configurable via JOB_GC_SCHEDULER_CRON).
 * While explicit GC calls are generally discouraged, they can be beneficial in
 * long-running containerized applications with predictable load patterns.
 */
@ApplicationScoped
public class GarbageCollectionJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(GarbageCollectionJob.class);
    private static final double BYTES_TO_MB = 1024.0 * 1024.0;
    
    private final int warningThreshold;
    private final int criticalThreshold;

    public GarbageCollectionJob(
            @ConfigProperty(name = "memory.warning.threshold.percent", defaultValue = "80") int warningThreshold,
            @ConfigProperty(name = "memory.critical.threshold.percent", defaultValue = "90") int criticalThreshold) {
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    /**
     * Executes a full garbage collection and logs memory statistics.
     * Runs on a configurable cron schedule (default: hourly).
     */
    @Scheduled(cron = "{scheduler.gc.cron}")
    public void execute() {
        try {
            Runtime runtime = Runtime.getRuntime();
            
            // Capture memory state before GC
            long usedMemoryBeforeMB = getUsedMemoryMB(runtime);
            long maxMemoryMB = runtime.maxMemory() / (long)BYTES_TO_MB;
            int usagePercentBefore = (int)((usedMemoryBeforeMB * 100) / maxMemoryMB);
            
            LOGGER.info("Memory before GC: {} MB / {} MB ({}% used)", 
                    usedMemoryBeforeMB, maxMemoryMB, usagePercentBefore);
            
            // Check memory thresholds before GC
            checkMemoryThresholds(usagePercentBefore, maxMemoryMB, usedMemoryBeforeMB);
            
            // Request garbage collection
            long startTime = System.currentTimeMillis();
            System.gc();
            long gcDuration = System.currentTimeMillis() - startTime;
            
            // Give GC some time to complete
            Thread.sleep(100);
            
            // Capture memory state after GC
            long usedMemoryAfterMB = getUsedMemoryMB(runtime);
            long freedMemoryMB = usedMemoryBeforeMB - usedMemoryAfterMB;
            int usagePercentAfter = (int)((usedMemoryAfterMB * 100) / maxMemoryMB);
            
            LOGGER.info("Memory after GC: {} MB / {} MB ({}% used). Freed: {} MB. Duration: {} ms", 
                    usedMemoryAfterMB, maxMemoryMB, usagePercentAfter, freedMemoryMB, gcDuration);
            
            // Check if memory is still high after GC
            if (usagePercentAfter > criticalThreshold) {
                LOGGER.error("CRITICAL: Memory usage remains high after GC: {}% used. Potential memory leak!", 
                        usagePercentAfter);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("GC job interrupted: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error during garbage collection execution: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Gets the currently used memory in megabytes.
     */
    private long getUsedMemoryMB(Runtime runtime) {
        return (runtime.totalMemory() - runtime.freeMemory()) / (long)BYTES_TO_MB;
    }
    
    /**
     * Checks memory usage against configured thresholds and logs warnings.
     */
    private void checkMemoryThresholds(int usagePercent, long maxMemoryMB, long usedMemoryMB) {
        if (usagePercent >= criticalThreshold) {
            LOGGER.error("CRITICAL: Memory usage at {}% ({}/{} MB). Immediate GC required!", 
                    usagePercent, usedMemoryMB, maxMemoryMB);
        } else if (usagePercent >= warningThreshold) {
            LOGGER.warn("WARNING: Memory usage at {}% ({}/{} MB). Approaching limit.", 
                    usagePercent, usedMemoryMB, maxMemoryMB);
        }
    }
}
