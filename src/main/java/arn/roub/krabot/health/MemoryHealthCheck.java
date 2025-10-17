package arn.roub.krabot.health;

import arn.roub.krabot.memory.MemoryMonitoringService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

/**
 * Health check that monitors memory usage and reports if memory is critically high.
 * This is a liveness probe - if memory is too high, the container should be restarted.
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
        long maxMemoryMB = runtime.maxMemory() / (long)BYTES_TO_MB;
        long totalMemoryMB = runtime.totalMemory() / (long)BYTES_TO_MB;
        long freeMemoryMB = runtime.freeMemory() / (long)BYTES_TO_MB;
        long usedMemoryMB = totalMemoryMB - freeMemoryMB;
        int usagePercent = memoryMonitoringService.getCurrentMemoryUsagePercent();
        
        HealthCheckResponseBuilder builder = HealthCheckResponse.named(HEALTH_CHECK_NAME)
                .withData("max_memory_mb", maxMemoryMB)
                .withData("used_memory_mb", usedMemoryMB)
                .withData("free_memory_mb", freeMemoryMB)
                .withData("usage_percent", usagePercent);
        
        // Fail health check if memory is critically high
        // This will trigger container restart in Kubernetes
        if (memoryMonitoringService.isMemoryAboveCriticalThreshold()) {
            return builder
                    .down()
                    .withData("status", "CRITICAL - Memory usage too high")
                    .build();
        }
        
        // Report warning if memory is above warning threshold but not critical
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
