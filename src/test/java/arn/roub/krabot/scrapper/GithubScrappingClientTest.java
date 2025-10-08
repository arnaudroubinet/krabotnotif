package arn.roub.krabot.scrapper;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for GithubScrappingClient.
 * 
 * WARNING: These tests make real HTTP calls to GitHub API.
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
@DisplayName("GithubScrappingClient Integration Tests")
class GithubScrappingClientTest {

    @Inject
    GithubScrappingClient client;

    @Test
    @DisplayName("Should construct client without errors")
    void shouldConstructClient() {
        assertNotNull(client);
    }

    @Test
    @DisplayName("Should retrieve latest release tag from GitHub")
    void shouldRetrieveLatestReleaseTag() {
        // When
        String tag = client.getLastReleaseTag();

        // Then
        assertNotNull(tag);
        assertFalse(tag.isEmpty());
        assertTrue(tag.startsWith("v") || tag.matches("\\d+\\.\\d+\\.\\d+"));
    }

    @Test
    @DisplayName("Should return consistent tag format")
    void shouldReturnConsistentTagFormat() {
        // When
        String tag1 = client.getLastReleaseTag();
        String tag2 = client.getLastReleaseTag();

        // Then - Multiple calls should return the same tag
        assertEquals(tag1, tag2);
    }

    @Test
    @DisplayName("Should handle GitHub API responses correctly")
    void shouldHandleGithubApiResponses() {
        // When
        String tag = assertDoesNotThrow(() -> client.getLastReleaseTag());

        // Then
        assertNotNull(tag);
        assertFalse(tag.isBlank());
    }
}
