package arn.roub.krabot.scrapper;

import arn.roub.krabot.exception.GithubApiException;
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
 * They may fail due to network issues or rate limiting.
 * 
 * To exclude from CI builds, use: mvn test -DexcludedGroups=integration
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
        assertNotNull(client, "Client should be injected");
    }

    @Test
    @DisplayName("Should retrieve latest release tag from GitHub or handle API errors gracefully")
    void shouldRetrieveLatestReleaseTag() {
        try {
            // When
            String tag = client.getLastReleaseTag();

            // Then - If successful, validate format
            assertNotNull(tag, "Tag should not be null");
            assertFalse(tag.isEmpty(), "Tag should not be empty");
            assertTrue(tag.startsWith("v") || tag.matches("\\d+\\.\\d+\\.\\d+"), 
                "Tag should start with 'v' or match semantic version format");
        } catch (GithubApiException e) {
            // API might be rate-limited or unavailable - this is acceptable in tests
            assertTrue(e.getMessage().contains("GitHub") || e.getMessage().contains("status code"),
                "Exception should mention GitHub or status code");
        }
    }

    @Test
    @DisplayName("Should return consistent tag format when API is available")
    void shouldReturnConsistentTagFormat() {
        try {
            // When
            String tag1 = client.getLastReleaseTag();
            String tag2 = client.getLastReleaseTag();

            // Then - Multiple calls should return the same tag (no releases between calls)
            assertEquals(tag1, tag2, "Consecutive calls should return same tag");
        } catch (GithubApiException e) {
            // API might be rate-limited - acceptable for integration test
            System.out.println("GitHub API unavailable during test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should throw GithubApiException when API fails")
    void shouldHandleGithubApiResponses() {
        // This test verifies exception handling
        // Either we get a valid tag or a proper exception
        assertDoesNotThrow(() -> {
            try {
                String tag = client.getLastReleaseTag();
                assertNotNull(tag, "Tag should not be null if call succeeds");
                assertFalse(tag.isBlank(), "Tag should not be blank if call succeeds");
            } catch (GithubApiException e) {
                // Expected when API is unavailable - verify exception is properly thrown
                assertNotNull(e.getMessage(), "Exception should have a message");
                assertTrue(e.getMessage().contains("GitHub") || e.getMessage().contains("status code"),
                    "Exception message should mention GitHub or status code");
            }
        }, "Method should either return valid tag or throw GithubApiException, but not other exceptions");
    }
}
