package arn.roub.krabot.domain.model;

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
        KramailId id = new KramailId("km123");
        String title = "Important Message";
        String originator = "Admin";
        String recipient = "User";

        // When
        Kramail kramail = new Kramail(id, title, originator, recipient);

        // Then
        assertEquals(id, kramail.id());
        assertEquals(title, kramail.title());
        assertEquals(originator, kramail.originator());
        assertEquals(recipient, kramail.recipient());
    }

    @Test
    @DisplayName("Should support equality based on all fields")
    void shouldSupportEquality() {
        // Given
        Kramail kramail1 = new Kramail(new KramailId("km1"), "Title", "Sender", "Recipient");
        Kramail kramail2 = new Kramail(new KramailId("km1"), "Title", "Sender", "Recipient");
        Kramail kramail3 = new Kramail(new KramailId("km2"), "Title", "Sender", "Recipient");

        // Then
        assertEquals(kramail1, kramail2);
        assertNotEquals(kramail1, kramail3);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void shouldHaveConsistentHashCode() {
        // Given
        Kramail kramail1 = new Kramail(new KramailId("km1"), "Title", "Sender", "Recipient");
        Kramail kramail2 = new Kramail(new KramailId("km1"), "Title", "Sender", "Recipient");

        // Then
        assertEquals(kramail1.hashCode(), kramail2.hashCode());
    }

    @Test
    @DisplayName("Should throw exception for null id")
    void shouldThrowExceptionForNullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new Kramail(null, "Title", "Sender", "Recipient")
        );
    }
}
