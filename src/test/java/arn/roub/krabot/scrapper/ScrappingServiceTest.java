package arn.roub.krabot.scrapper;

import arn.roub.krabot.config.DiscordConfig;
import arn.roub.krabot.utils.DiscordWebhook;
import arn.roub.krabot.utils.DiscordWebhookFactory;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ScrappingService with mocked Discord webhooks.
 * Uses DiscordWebhookFactory for proper mocking without actual HTTP calls.
 */
@QuarkusTest
@DisplayName("ScrappingService with Mocked Discord")
class ScrappingServiceTest {

    @Inject
    ScrappingService scrappingService;

    @Inject
    CurrentState currentState;

    @InjectMock
    KralandScrappingClient kralandScrappingClient;

    @InjectMock
    GithubScrappingClient githubScrappingClient;

    @InjectMock
    DiscordWebhookFactory webhookFactory;

    @Inject
    DiscordConfig discordConfig;

    private DiscordWebhook mockWebhook;

    @BeforeEach
    void setupMocks() throws IOException {
        // Reset state
        currentState.setHasNotification(false);
        currentState.setNbkramail(0);
        currentState.setLatestVersion(null);

        // Create and configure mock webhook
        mockWebhook = mock(DiscordWebhook.class);
        doNothing().when(mockWebhook).execute();
        doNothing().when(mockWebhook).setAvatarUrl(anyString());
        doNothing().when(mockWebhook).setUsername(anyString());
        doNothing().when(mockWebhook).setContent(anyString());
        doNothing().when(mockWebhook).setTts(anyBoolean());

        // Factory returns our mock
        when(webhookFactory.create(anyString())).thenReturn(mockWebhook);

        // Clear any previous invocations
        clearInvocations(githubScrappingClient, kralandScrappingClient, webhookFactory, mockWebhook);
    }

    @Test
    @DisplayName("Should handle multiple kramails")
    void shouldHandleMultipleKramails() throws IOException {
        // Given
        List<Kramail> kramails = List.of(
                new Kramail("1", "Title1", "Sender1"),
                new Kramail("2", "Title2", "Sender2")
        );
        when(kralandScrappingClient.hasNotification(anyString(), anyString()))
                .thenReturn(new ScrappingResponse(kramails, false));

        // When
        scrappingService.loadKiAndSendNotificationIfWeHaveReport();

        // Then
        assertEquals(2, currentState.getNbkramail());
        // Webhook called for each kramail
        verify(mockWebhook, atLeast(2)).execute();
    }

    @Test
    @DisplayName("Should remove old kramails")
    void shouldRemoveOldKramails() throws IOException {
        // Given - First call with 2 kramails
        List<Kramail> initial = List.of(
                new Kramail("1", "Title1", "Sender1"),
                new Kramail("2", "Title2", "Sender2")
        );
        when(kralandScrappingClient.hasNotification(anyString(), anyString()))
                .thenReturn(new ScrappingResponse(initial, false));
        scrappingService.loadKiAndSendNotificationIfWeHaveReport();

        // When - Second call with only 1 kramail
        List<Kramail> reduced = List.of(new Kramail("1", "Title1", "Sender1"));
        when(kralandScrappingClient.hasNotification(anyString(), anyString()))
                .thenReturn(new ScrappingResponse(reduced, false));
        scrappingService.loadKiAndSendNotificationIfWeHaveReport();

        // Then
        assertEquals(1, currentState.getNbkramail());
    }

    @Test
    @DisplayName("Discord webhook is properly mocked - no real HTTP calls")
    void discordWebhookIsMocked() throws IOException {
        // This test verifies the mocking setup works
        // If Discord webhook made real HTTP calls, this would fail with network errors

        // Given
        List<Kramail> kramails = List.of(new Kramail("1", "Test", "Sender"));
        when(kralandScrappingClient.hasNotification(anyString(), anyString()))
                .thenReturn(new ScrappingResponse(kramails, true));

        // When - This would fail if real HTTP calls were made
        assertDoesNotThrow(() -> scrappingService.loadKiAndSendNotificationIfWeHaveReport());

        // Then - Verify webhook was "called" via mock
        verify(webhookFactory, atLeastOnce()).create(discordConfig.url());
        verify(mockWebhook, atLeastOnce()).execute();
    }
}
