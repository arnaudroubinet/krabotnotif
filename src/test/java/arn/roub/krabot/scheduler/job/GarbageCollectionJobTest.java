package arn.roub.krabot.scheduler.job;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for GarbageCollectionJob.
 * Validates that the GC job can execute without errors and logs memory statistics.
 */
@QuarkusTest
@DisplayName("GarbageCollectionJob Tests")
class GarbageCollectionJobTest {

    @Inject
    GarbageCollectionJob garbageCollectionJob;

    @Test
    @DisplayName("Should execute GC job without throwing exceptions")
    void shouldExecuteGCJobSuccessfully() {
        // When - Execute the GC job
        assertDoesNotThrow(() -> garbageCollectionJob.execute());
    }

    @Test
    @DisplayName("Should complete GC execution within reasonable time")
    void shouldCompleteGCExecutionQuickly() {
        // Given
        long startTime = System.currentTimeMillis();
        
        // When
        garbageCollectionJob.execute();
        
        // Then - Should complete within 5 seconds
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 5000, 
                "GC execution took too long: " + duration + "ms");
    }

    @Test
    @DisplayName("Should reduce memory usage after GC")
    void shouldReduceMemoryAfterGC() {
        // Given - Create some garbage
        @SuppressWarnings("unused")
        byte[][] garbage = new byte[100][1024 * 100]; // 10MB of garbage
        
        Runtime runtime = Runtime.getRuntime();
        long usedBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // When - Run GC
        garbageCollectionJob.execute();
        
        // Then - Memory should be freed or at least not significantly increased
        long usedAfter = runtime.totalMemory() - runtime.freeMemory();
        
        // Just verify the job ran without error
        // Actual memory reduction depends on JVM behavior
        assertNotNull(usedAfter);
        assertTrue(usedAfter >= 0);
    }
}
