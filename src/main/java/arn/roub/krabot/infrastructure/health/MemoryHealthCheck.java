package arn.roub.krabot.infrastructure.health;

import arn.roub.krabot.shared.memory.MemoryMonitoringService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

/**
 * Health check pour le monitoring de la mémoire.
 */
@Liveness
@ApplicationScoped
public class MemoryHealthCheck implements HealthCheck {

    private static final String HEALTH_CHECK_NAME = "Memory Usage Health Check";
    private static final double BYTES_TO_MB = 1024.0 * 1024.0;

    private final MemoryMonitoringService memoryMonitoringService;

    public MemoryHealthCheck(MemoryMonitoringService memoryMonitoringService) {
        this.memoryMonitoringService = memoryMonitoringService;
    }

    @Override
    public HealthCheckResponse call() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemoryMB = runtime.maxMemory() / (long) BYTES_TO_MB;
        long totalMemoryMB = runtime.totalMemory() / (long) BYTES_TO_MB;
        long freeMemoryMB = runtime.freeMemory() / (long) BYTES_TO_MB;
        long usedMemoryMB = totalMemoryMB - freeMemoryMB;
        int usagePercent = memoryMonitoringService.getCurrentMemoryUsagePercent();

        HealthCheckResponseBuilder builder = HealthCheckResponse.named(HEALTH_CHECK_NAME)
                .withData("max_memory_mb", maxMemoryMB)
                .withData("used_memory_mb", usedMemoryMB)
                .withData("free_memory_mb", freeMemoryMB)
                .withData("usage_percent", usagePercent);

        if (memoryMonitoringService.isMemoryAboveCriticalThreshold()) {
            return builder
                    .down()
                    .withData("status", "CRITICAL - Memory usage too high")
                    .build();
        }

        if (memoryMonitoringService.isMemoryAboveWarningThreshold()) {
            return builder
                    .up()
                    .withData("status", "WARNING - Memory usage high")
                    .build();
        }

        return builder
                .up()
                .withData("status", "OK - Memory usage normal")
                .build();
    }
}
