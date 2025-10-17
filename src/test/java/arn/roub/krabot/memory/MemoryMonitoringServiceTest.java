package arn.roub.krabot.memory;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for MemoryMonitoringService.
 * Validates memory monitoring functionality and threshold checking.
 */
@QuarkusTest
@DisplayName("MemoryMonitoringService Tests")
class MemoryMonitoringServiceTest {

    @Inject
    MemoryMonitoringService memoryMonitoringService;

    @Test
    @DisplayName("Should get current memory usage percentage")
    void shouldGetCurrentMemoryUsagePercent() {
        // When
        int usagePercent = memoryMonitoringService.getCurrentMemoryUsagePercent();
        
        // Then - Should be a valid percentage
        assertTrue(usagePercent >= 0 && usagePercent <= 100,
                "Memory usage should be between 0 and 100%, got: " + usagePercent);
    }

    @Test
    @DisplayName("Should check warning threshold correctly")
    void shouldCheckWarningThreshold() {
        // When
        boolean aboveWarning = memoryMonitoringService.isMemoryAboveWarningThreshold();
        boolean aboveCritical = memoryMonitoringService.isMemoryAboveCriticalThreshold();
        
        // Then - Thresholds should be logical
        if (aboveCritical) {
            // If critical, then also above warning
            assertTrue(aboveWarning, "If above critical threshold, must also be above warning");
        }
        
        // Just verify method doesn't throw
        assertNotNull(aboveWarning);
        assertNotNull(aboveCritical);
    }

    @Test
    @DisplayName("Should handle memory monitoring initialization")
    void shouldInitializeSuccessfully() {
        // Given - Service is already initialized by Quarkus
        
        // When - We check memory usage
        int usagePercent = memoryMonitoringService.getCurrentMemoryUsagePercent();
        
        // Then - Should return valid value
        assertTrue(usagePercent >= 0, "Memory usage cannot be negative");
    }

    @Test
    @DisplayName("Should report consistent memory state")
    void shouldReportConsistentMemoryState() {
        // When - Check multiple times
        int usage1 = memoryMonitoringService.getCurrentMemoryUsagePercent();
        int usage2 = memoryMonitoringService.getCurrentMemoryUsagePercent();
        
        // Then - Should be relatively consistent (within 20% difference)
        int difference = Math.abs(usage1 - usage2);
        assertTrue(difference < 20, 
                "Memory usage should be relatively stable, difference was: " + difference + "%");
    }
}
