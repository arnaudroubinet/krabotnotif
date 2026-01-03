package arn.roub.krabot.infrastructure.adapter.in.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler pour le garbage collection explicite.
 */
@ApplicationScoped
public class GarbageCollectionScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GarbageCollectionScheduler.class);
    private static final double BYTES_TO_MB = 1024.0 * 1024.0;

    private final int warningThreshold;
    private final int criticalThreshold;

    public GarbageCollectionScheduler(
            @ConfigProperty(name = "memory.warning.threshold.percent", defaultValue = "80") int warningThreshold,
            @ConfigProperty(name = "memory.critical.threshold.percent", defaultValue = "90") int criticalThreshold
    ) {
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    @Scheduled(cron = "${scheduler.gc.cron}")
    public void execute() {
        try {
            Runtime runtime = Runtime.getRuntime();

            long usedMemoryBeforeMB = getUsedMemoryMB(runtime);
            long maxMemoryMB = runtime.maxMemory() / (long) BYTES_TO_MB;
            int usagePercentBefore = (int) ((usedMemoryBeforeMB * 100) / maxMemoryMB);

            LOGGER.info("Memory before GC: {} MB / {} MB ({}% used)",
                    usedMemoryBeforeMB, maxMemoryMB, usagePercentBefore);

            checkMemoryThresholds(usagePercentBefore, maxMemoryMB, usedMemoryBeforeMB);

            long startTime = System.currentTimeMillis();
            System.gc();
            long gcDuration = System.currentTimeMillis() - startTime;

            Thread.sleep(100);

            long usedMemoryAfterMB = getUsedMemoryMB(runtime);
            long freedMemoryMB = usedMemoryBeforeMB - usedMemoryAfterMB;
            int usagePercentAfter = (int) ((usedMemoryAfterMB * 100) / maxMemoryMB);

            LOGGER.info("Memory after GC: {} MB / {} MB ({}% used). Freed: {} MB. Duration: {} ms",
                    usedMemoryAfterMB, maxMemoryMB, usagePercentAfter, freedMemoryMB, gcDuration);

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

    private long getUsedMemoryMB(Runtime runtime) {
        return (runtime.totalMemory() - runtime.freeMemory()) / (long) BYTES_TO_MB;
    }

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
