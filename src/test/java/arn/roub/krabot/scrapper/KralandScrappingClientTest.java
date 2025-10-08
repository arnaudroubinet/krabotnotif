package arn.roub.krabot.scrapper;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for KralandScrappingClient.
 * 
 * WARNING: These tests make real HTTP calls to Kraland website.
 * They are tagged as "integration" and can be excluded from unit test runs.
 * 
 * To run only unit tests: mvn test -DexcludedGroups=integration
 * To run all tests: mvn test
 * 
 * RECOMMENDATION: Consider using WireMock or similar for true unit testing
 * without external dependencies.
 */
@QuarkusTest
@Tag("integration")
@DisplayName("KralandScrappingClient Integration Tests")
class KralandScrappingClientTest {

    @Inject
    KralandScrappingClient client;

    @Test
    @DisplayName("Should construct client without errors")
    void shouldConstructClient() {
        assertNotNull(client);
    }

    @Test
    @DisplayName("Should handle invalid credentials without crashing")
    void shouldHandleInvalidCredentials() {
        // Given
        String invalidUser = "invalid_user_that_does_not_exist_12345";
        String invalidPassword = "invalid_pass";

        // When & Then - Should not crash, may return empty response
        assertDoesNotThrow(() -> {
            ScrappingResponse response = client.hasNotification(invalidUser, invalidPassword);
            assertNotNull(response);
            assertNotNull(response.kramails());
        });
    }

    @Test
    @DisplayName("Should handle password truncation for long passwords")
    void shouldTruncateLongPasswords() {
        // Given
        String user = "testuser";
        String longPassword = "verylongpasswordthatexceedslimit123456789";

        // When & Then - Should not crash with long password
        assertDoesNotThrow(() -> client.hasNotification(user, longPassword));
    }

    @Test
    @DisplayName("Should return valid ScrappingResponse structure")
    void shouldReturnValidResponseStructure() {
        // Given
        String testUser = "testuser";
        String testPassword = "testpass";

        // When
        ScrappingResponse response = client.hasNotification(testUser, testPassword);

        // Then
        assertNotNull(response);
        assertNotNull(response.kramails());
    }
}
