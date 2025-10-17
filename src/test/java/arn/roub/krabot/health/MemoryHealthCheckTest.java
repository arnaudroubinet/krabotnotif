package arn.roub.krabot.health;

import arn.roub.krabot.memory.MemoryMonitoringService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for MemoryHealthCheck.
 * Validates that the health check properly reports memory status.
 */
@QuarkusTest
@DisplayName("MemoryHealthCheck Tests")
class MemoryHealthCheckTest {

    @Inject
    @Liveness
    MemoryHealthCheck memoryHealthCheck;

    @Inject
    MemoryMonitoringService memoryMonitoringService;

    @Test
    @DisplayName("Should return health check response")
    void shouldReturnHealthCheckResponse() {
        // When
        HealthCheckResponse response = memoryHealthCheck.call();
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getName());
        assertEquals("Memory Usage Health Check", response.getName());
    }

    @Test
    @DisplayName("Should include memory data in response")
    void shouldIncludeMemoryDataInResponse() {
        // When
        HealthCheckResponse response = memoryHealthCheck.call();
        
        // Then - Should contain memory metrics
        assertTrue(response.getData().isPresent());
        var data = response.getData().get();
        
        assertTrue(data.containsKey("max_memory_mb"));
        assertTrue(data.containsKey("used_memory_mb"));
        assertTrue(data.containsKey("free_memory_mb"));
        assertTrue(data.containsKey("usage_percent"));
        assertTrue(data.containsKey("status"));
    }

    @Test
    @DisplayName("Should report UP status when memory is normal")
    void shouldReportUpWhenMemoryNormal() {
        // When
        HealthCheckResponse response = memoryHealthCheck.call();
        
        // Then - Under normal conditions, should be UP
        // (unless running in extremely constrained environment)
        int usagePercent = memoryMonitoringService.getCurrentMemoryUsagePercent();
        
        if (usagePercent < 90) {
            // Should be up if below critical threshold
            assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        }
        
        // At minimum, response should not be null
        assertNotNull(response.getStatus());
    }

    @Test
    @DisplayName("Should have valid memory values")
    void shouldHaveValidMemoryValues() {
        // When
        HealthCheckResponse response = memoryHealthCheck.call();
        
        // Then
        var data = response.getData().get();
        
        long maxMemory = (Long) data.get("max_memory_mb");
        long usedMemory = (Long) data.get("used_memory_mb");
        long freeMemory = (Long) data.get("free_memory_mb");
        int usagePercent = ((Number) data.get("usage_percent")).intValue();
        
        assertTrue(maxMemory > 0, "Max memory should be positive");
        assertTrue(usedMemory >= 0, "Used memory should be non-negative");
        assertTrue(freeMemory >= 0, "Free memory should be non-negative");
        assertTrue(usagePercent >= 0 && usagePercent <= 100, 
                "Usage percent should be between 0 and 100");
    }
}
