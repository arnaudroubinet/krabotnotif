package arn.roub.krabot.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScrapingResult record.
 * Tests the immutable response structure from scraping operations.
 */
@DisplayName("ScrapingResult Tests")
class ScrapingResultTest {

    @Test
    @DisplayName("Should create result with kramails and notification flag")
    void shouldCreateResult() {
        // Given
        List<Kramail> kramails = List.of(
                new Kramail(new KramailId("km1"), "Title 1", "Sender1", "Recipient1"),
                new Kramail(new KramailId("km2"), "Title 2", "Sender2", "Recipient2")
        );
        boolean hasNotification = true;

        // When
        ScrapingResult result = new ScrapingResult(kramails, hasNotification);

        // Then
        assertEquals(kramails, result.kramails());
        assertTrue(result.hasNotification());
        assertTrue(result.hasKramails());
    }

    @Test
    @DisplayName("Should create result with empty kramails list")
    void shouldCreateResultWithEmptyKramails() {
        // Given
        List<Kramail> emptyList = List.of();

        // When
        ScrapingResult result = new ScrapingResult(emptyList, false);

        // Then
        assertTrue(result.kramails().isEmpty());
        assertFalse(result.hasNotification());
        assertFalse(result.hasKramails());
    }

    @Test
    @DisplayName("Should create empty result using factory method")
    void shouldCreateEmptyResult() {
        // When
        ScrapingResult result = ScrapingResult.empty();

        // Then
        assertTrue(result.kramails().isEmpty());
        assertFalse(result.hasNotification());
        assertFalse(result.hasKramails());
    }

    @Test
    @DisplayName("Should support equality based on all fields")
    void shouldSupportEquality() {
        // Given
        List<Kramail> kramails1 = List.of(new Kramail(new KramailId("km1"), "Title", "Sender", "Recipient"));
        List<Kramail> kramails2 = List.of(new Kramail(new KramailId("km1"), "Title", "Sender", "Recipient"));

        ScrapingResult result1 = new ScrapingResult(kramails1, true);
        ScrapingResult result2 = new ScrapingResult(kramails2, true);
        ScrapingResult result3 = new ScrapingResult(kramails1, false);

        // Then
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void shouldHaveConsistentHashCode() {
        // Given
        List<Kramail> kramails = List.of(new Kramail(new KramailId("km1"), "Title", "Sender", "Recipient"));
        ScrapingResult result1 = new ScrapingResult(kramails, true);
        ScrapingResult result2 = new ScrapingResult(kramails, true);

        // Then
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should handle notification flag independently from kramails")
    void shouldHandleNotificationFlagIndependently() {
        // Scenario 1: Has notification but no kramails
        ScrapingResult result1 = new ScrapingResult(List.of(), true);
        assertTrue(result1.hasNotification());
        assertTrue(result1.kramails().isEmpty());

        // Scenario 2: Has kramails but no notification
        List<Kramail> kramails = List.of(new Kramail(new KramailId("km1"), "Title", "Sender", "Recipient"));
        ScrapingResult result2 = new ScrapingResult(kramails, false);
        assertFalse(result2.hasNotification());
        assertEquals(1, result2.kramails().size());

        // Scenario 3: Both kramails and notification
        ScrapingResult result3 = new ScrapingResult(kramails, true);
        assertTrue(result3.hasNotification());
        assertEquals(1, result3.kramails().size());
    }

    @Test
    @DisplayName("Should handle null kramails list gracefully")
    void shouldHandleNullKramails() {
        // When
        ScrapingResult result = new ScrapingResult(null, false);

        // Then
        assertNotNull(result.kramails());
        assertTrue(result.kramails().isEmpty());
    }
}
