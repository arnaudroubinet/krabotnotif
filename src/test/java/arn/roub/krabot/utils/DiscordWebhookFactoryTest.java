package arn.roub.krabot.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for DiscordWebhookFactory.
 * Tests the factory that creates DiscordWebhook instances.
 */
@DisplayName("DiscordWebhookFactory Tests")
class DiscordWebhookFactoryTest {

    @Test
    @DisplayName("Should create DiscordWebhook instance")
    void shouldCreateWebhookInstance() {
        // Given
        DiscordWebhookFactory factory = new DiscordWebhookFactory();
        String testUrl = "https://discord.com/api/webhooks/test";

        // When
        DiscordWebhook webhook = factory.create(testUrl);

        // Then
        assertNotNull(webhook);
    }

    @Test
    @DisplayName("Should create new instances for each call")
    void shouldCreateNewInstances() {
        // Given
        DiscordWebhookFactory factory = new DiscordWebhookFactory();
        String testUrl = "https://discord.com/api/webhooks/test";

        // When
        DiscordWebhook webhook1 = factory.create(testUrl);
        DiscordWebhook webhook2 = factory.create(testUrl);

        // Then
        assertNotNull(webhook1);
        assertNotNull(webhook2);
        assertNotSame(webhook1, webhook2);
    }

    @Test
    @DisplayName("Should handle different URLs")
    void shouldHandleDifferentUrls() {
        // Given
        DiscordWebhookFactory factory = new DiscordWebhookFactory();
        String url1 = "https://discord.com/api/webhooks/test1";
        String url2 = "https://discord.com/api/webhooks/test2";

        // When
        DiscordWebhook webhook1 = factory.create(url1);
        DiscordWebhook webhook2 = factory.create(url2);

        // Then
        assertNotNull(webhook1);
        assertNotNull(webhook2);
    }
}
