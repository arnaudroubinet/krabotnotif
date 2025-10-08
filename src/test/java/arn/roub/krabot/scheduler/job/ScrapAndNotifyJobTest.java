package arn.roub.krabot.scheduler.job;

import arn.roub.krabot.errors.ExceptionNotificationService;
import arn.roub.krabot.exception.DiscordNotificationException;
import arn.roub.krabot.scrapper.ScrappingService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ScrapAndNotifyJob.
 * Tests the scheduled job that scrapes Kraland for notifications.
 */
@QuarkusTest
@DisplayName("ScrapAndNotifyJob Tests")
class ScrapAndNotifyJobTest {

    @Inject
    ScrapAndNotifyJob job;

    @InjectMock
    ScrappingService scrappingService;

    @InjectMock
    ExceptionNotificationService exceptionNotificationService;

    @Test
    @DisplayName("Should execute successfully when no errors occur")
    void shouldExecuteSuccessfully() {
        // Given
        doNothing().when(scrappingService).loadKiAndSendNotificationIfWeHaveReport();

        // When & Then
        assertDoesNotThrow(() -> job.execute());
        verify(scrappingService, times(1)).loadKiAndSendNotificationIfWeHaveReport();
        verify(exceptionNotificationService, never()).exceptionManagement(any());
    }

    @Test
    @DisplayName("Should handle exceptions and notify via Discord")
    void shouldHandleExceptionsAndNotify() {
        // Given
        RuntimeException testException = new RuntimeException("Test error");
        doThrow(testException).when(scrappingService).loadKiAndSendNotificationIfWeHaveReport();
        doNothing().when(exceptionNotificationService).exceptionManagement(any());

        // When & Then - Should not propagate exception
        assertDoesNotThrow(() -> job.execute());
        verify(exceptionNotificationService, times(1)).exceptionManagement(testException);
    }

    @Test
    @DisplayName("Should handle notification failure gracefully")
    void shouldHandleNotificationFailure() {
        // Given
        RuntimeException scrapException = new RuntimeException("Scraping failed");
        doThrow(scrapException).when(scrappingService).loadKiAndSendNotificationIfWeHaveReport();
        doThrow(new DiscordNotificationException("Discord failed"))
            .when(exceptionNotificationService).exceptionManagement(any());

        // When & Then - Should not propagate exception
        assertDoesNotThrow(() -> job.execute());
        verify(exceptionNotificationService, times(1)).exceptionManagement(scrapException);
    }

    @Test
    @DisplayName("Should add notification failure as suppressed exception")
    void shouldAddNotificationFailureAsSuppressedException() {
        // Given
        RuntimeException scrapException = new RuntimeException("Scraping failed");
        RuntimeException notificationException = new DiscordNotificationException("Discord notification failed");
        
        doThrow(scrapException).when(scrappingService).loadKiAndSendNotificationIfWeHaveReport();
        doThrow(notificationException).when(exceptionNotificationService).exceptionManagement(scrapException);

        // When
        job.execute();

        // Then - Verify suppressed exception was added
        Throwable[] suppressed = scrapException.getSuppressed();
        assertEquals(1, suppressed.length, "Should have exactly one suppressed exception");
        assertSame(notificationException, suppressed[0], "Suppressed exception should be the notification exception");
        verify(exceptionNotificationService, times(1)).exceptionManagement(scrapException);
    }
}
