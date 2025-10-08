package arn.roub.krabot.scrapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Kramail record.
 * Tests the immutable data structure for kramail messages.
 */
@DisplayName("Kramail Tests")
class KramailTest {

    @Test
    @DisplayName("Should create kramail with all fields")
    void shouldCreateKramailWithAllFields() {
        // Given
        String id = "km123";
        String title = "Important Message";
        String originator = "Admin";

        // When
        Kramail kramail = new Kramail(id, title, originator);

        // Then
        assertEquals(id, kramail.id());
        assertEquals(title, kramail.title());
        assertEquals(originator, kramail.originator());
    }

    @Test
    @DisplayName("Should support equality based on all fields")
    void shouldSupportEquality() {
        // Given
        Kramail kramail1 = new Kramail("km1", "Title", "Sender");
        Kramail kramail2 = new Kramail("km1", "Title", "Sender");
        Kramail kramail3 = new Kramail("km2", "Title", "Sender");

        // Then
        assertEquals(kramail1, kramail2);
        assertNotEquals(kramail1, kramail3);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void shouldHaveConsistentHashCode() {
        // Given
        Kramail kramail1 = new Kramail("km1", "Title", "Sender");
        Kramail kramail2 = new Kramail("km1", "Title", "Sender");

        // Then
        assertEquals(kramail1.hashCode(), kramail2.hashCode());
    }
}
