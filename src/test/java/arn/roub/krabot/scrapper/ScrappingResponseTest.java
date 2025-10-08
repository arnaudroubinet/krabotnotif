package arn.roub.krabot.scrapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScrappingResponse record.
 * Tests the immutable response structure from scraping operations.
 */
@DisplayName("ScrappingResponse Tests")
class ScrappingResponseTest {

    @Test
    @DisplayName("Should create response with kramails and notification flag")
    void shouldCreateResponse() {
        // Given
        List<Kramail> kramails = List.of(
                new Kramail("km1", "Title 1", "Sender1"),
                new Kramail("km2", "Title 2", "Sender2")
        );
        boolean hasNotification = true;

        // When
        ScrappingResponse response = new ScrappingResponse(kramails, hasNotification);

        // Then
        assertEquals(kramails, response.kramails());
        assertTrue(response.hasNotification());
    }

    @Test
    @DisplayName("Should create response with empty kramails list")
    void shouldCreateResponseWithEmptyKramails() {
        // Given
        List<Kramail> emptyList = List.of();

        // When
        ScrappingResponse response = new ScrappingResponse(emptyList, false);

        // Then
        assertTrue(response.kramails().isEmpty());
        assertFalse(response.hasNotification());
    }

    @Test
    @DisplayName("Should support equality based on all fields")
    void shouldSupportEquality() {
        // Given
        List<Kramail> kramails1 = List.of(new Kramail("km1", "Title", "Sender"));
        List<Kramail> kramails2 = List.of(new Kramail("km1", "Title", "Sender"));
        
        ScrappingResponse response1 = new ScrappingResponse(kramails1, true);
        ScrappingResponse response2 = new ScrappingResponse(kramails2, true);
        ScrappingResponse response3 = new ScrappingResponse(kramails1, false);

        // Then
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void shouldHaveConsistentHashCode() {
        // Given
        List<Kramail> kramails = List.of(new Kramail("km1", "Title", "Sender"));
        ScrappingResponse response1 = new ScrappingResponse(kramails, true);
        ScrappingResponse response2 = new ScrappingResponse(kramails, true);

        // Then
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("Should handle notification flag independently from kramails")
    void shouldHandleNotificationFlagIndependently() {
        // Scenario 1: Has notification but no kramails
        ScrappingResponse response1 = new ScrappingResponse(List.of(), true);
        assertTrue(response1.hasNotification());
        assertTrue(response1.kramails().isEmpty());

        // Scenario 2: Has kramails but no notification
        List<Kramail> kramails = List.of(new Kramail("km1", "Title", "Sender"));
        ScrappingResponse response2 = new ScrappingResponse(kramails, false);
        assertFalse(response2.hasNotification());
        assertEquals(1, response2.kramails().size());

        // Scenario 3: Both kramails and notification
        ScrappingResponse response3 = new ScrappingResponse(kramails, true);
        assertTrue(response3.hasNotification());
        assertEquals(1, response3.kramails().size());
    }
}
