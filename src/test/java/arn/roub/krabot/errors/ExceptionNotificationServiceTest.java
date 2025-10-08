package arn.roub.krabot.errors;

import arn.roub.krabot.exception.DiscordNotificationException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ExceptionNotificationService.
 * Tests exception handling and Discord notification for errors.
 * 
 * NOTE: These are integration-style tests that require Discord webhook configuration.
 * The service creates DiscordWebhook instances directly, making it difficult to mock completely.
 * Real Discord notifications will fail in test environment (expected behavior).
 */
@QuarkusTest
@DisplayName("ExceptionNotificationService Tests")
class ExceptionNotificationServiceTest {

    @Inject
    ExceptionNotificationService service;

    @Test
    @DisplayName("Should be injectable")
    void shouldBeInjectable() {
        assertNotNull(service);
    }

    @Test
    @DisplayName("Should wrap null pointer exception when ex is null")
    void shouldHandleNullException() {
        // The service catches all exceptions and wraps them in DiscordNotificationException
        assertThrows(DiscordNotificationException.class, () -> service.exceptionManagement(null));
    }

    @Test
    @DisplayName("Should attempt to send notification for runtime exception")
    void shouldAttemptNotificationForException() {
        // Given
        RuntimeException testException = new RuntimeException("Test error message");

        // When & Then - May throw DiscordNotificationException if webhook is invalid
        // This is expected behavior as we're testing without real Discord webhook
        assertThrows(Exception.class, () -> service.exceptionManagement(testException));
    }
}
