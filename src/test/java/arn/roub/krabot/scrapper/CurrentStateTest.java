package arn.roub.krabot.scrapper;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CurrentStateTest {

    @Inject
    CurrentState currentState;

    @Test
    void testCurrentVersionIsNotNull() {
        assertNotNull(currentState.getCurrentVersion());
    }

    @Test
    void testCurrentVersionIsNotEmpty() {
        assertFalse(currentState.getCurrentVersion().isEmpty());
    }

    @Test
    void testCurrentVersionFormat() {
        String version = currentState.getCurrentVersion();
        System.out.println("Current version: " + version);
        // Version should either be a git tag (starting with v), a commit hash, or the fallback format
        assertTrue(
                version.startsWith("v") || 
                version.matches("[0-9a-f]{7}") || 
                version.startsWith("v.") ||
                version.equals("Unknown"),
                "Version format should be valid: " + version
        );
    }

    @Test
    void testInitialState() {
        assertEquals(0, currentState.getNbkramail());
        assertFalse(currentState.getHasNotification());
        assertEquals("Unknown", currentState.getLatestVersion());
    }

    @Test
    void testSettersWork() {
        int originalKramail = currentState.getNbkramail();
        boolean originalNotification = currentState.getHasNotification();
        String originalLatestVersion = currentState.getLatestVersion();

        currentState.setNbkramail(5);
        currentState.setHasNotification(true);
        currentState.setLatestVersion("v2.0.5");

        assertEquals(5, currentState.getNbkramail());
        assertTrue(currentState.getHasNotification());
        assertEquals("v2.0.5", currentState.getLatestVersion());

        // Restore original values
        currentState.setNbkramail(originalKramail);
        currentState.setHasNotification(originalNotification);
        currentState.setLatestVersion(originalLatestVersion);
    }
}
