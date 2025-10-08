package arn.roub.krabot.scrapper;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CurrentState.
 * Tests the state management bean that tracks application state.
 */
@QuarkusTest
@DisplayName("CurrentState Tests")
class CurrentStateTest {

    @Inject
    CurrentState currentState;

    @BeforeEach
    void resetState() {
        currentState.setHasNotification(false);
        currentState.setNbkramail(0);
        currentState.setLatestVersion(null);
    }

    @Test
    @DisplayName("Should initialize with injected version")
    void shouldInitializeWithInjectedVersion() {
        // Then - Can't test constructor directly due to @ConfigProperty injection
        // This test verifies the injected bean is properly initialized
        assertNotNull(currentState);
        assertNotNull(currentState.getCurrentVersion());
        assertEquals(0, currentState.getNbkramail());
        assertFalse(currentState.getHasNotification());
    }

    @Test
    @DisplayName("Should set and get notification flag")
    void shouldSetAndGetNotificationFlag() {
        // When
        currentState.setHasNotification(true);

        // Then
        assertTrue(currentState.getHasNotification());

        // When
        currentState.setHasNotification(false);

        // Then
        assertFalse(currentState.getHasNotification());
    }

    @Test
    @DisplayName("Should set and get kramail count")
    void shouldSetAndGetKramailCount() {
        // When
        currentState.setNbkramail(5);

        // Then
        assertEquals(5, currentState.getNbkramail());

        // When
        currentState.setNbkramail(0);

        // Then
        assertEquals(0, currentState.getNbkramail());
    }

    @Test
    @DisplayName("Should handle large kramail counts")
    void shouldHandleLargeKramailCounts() {
        // When
        currentState.setNbkramail(9999);

        // Then
        assertEquals(9999, currentState.getNbkramail());
    }

    @Test
    @DisplayName("Should set and get latest version")
    void shouldSetAndGetLatestVersion() {
        // Given
        String version = "v2.4.10";

        // When
        currentState.setLatestVersion(version);

        // Then
        assertEquals(version, currentState.getLatestVersion());
    }

    @Test
    @DisplayName("Should handle null version")
    void shouldHandleNullVersion() {
        // When
        currentState.setLatestVersion("v1.0.0");
        currentState.setLatestVersion(null);

        // Then
        assertNull(currentState.getLatestVersion());
    }

    @Test
    @DisplayName("Should handle empty version string")
    void shouldHandleEmptyVersion() {
        // When
        currentState.setLatestVersion("");

        // Then
        assertEquals("", currentState.getLatestVersion());
    }

    @Test
    @DisplayName("Should maintain independent state values")
    void shouldMaintainIndependentState() {
        // When
        currentState.setHasNotification(true);
        currentState.setNbkramail(3);
        currentState.setLatestVersion("v2.0.0");

        // Then
        assertTrue(currentState.getHasNotification());
        assertEquals(3, currentState.getNbkramail());
        assertEquals("v2.0.0", currentState.getLatestVersion());

        // When - Change one value
        currentState.setHasNotification(false);

        // Then - Other values unchanged
        assertFalse(currentState.getHasNotification());
        assertEquals(3, currentState.getNbkramail());
        assertEquals("v2.0.0", currentState.getLatestVersion());
    }

    @Test
    @DisplayName("Should handle state transitions")
    void shouldHandleStateTransitions() {
        // Scenario: Bot starts, receives notifications, then clears them
        
        // Initial state
        assertFalse(currentState.getHasNotification());
        assertEquals(0, currentState.getNbkramail());

        // Notifications arrive
        currentState.setHasNotification(true);
        currentState.setNbkramail(5);
        assertTrue(currentState.getHasNotification());
        assertEquals(5, currentState.getNbkramail());

        // User reads some kramails
        currentState.setNbkramail(2);
        assertEquals(2, currentState.getNbkramail());

        // All notifications cleared
        currentState.setHasNotification(false);
        currentState.setNbkramail(0);
        assertFalse(currentState.getHasNotification());
        assertEquals(0, currentState.getNbkramail());
    }
}
