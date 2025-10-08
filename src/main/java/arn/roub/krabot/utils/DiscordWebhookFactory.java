package arn.roub.krabot.utils;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Factory for creating DiscordWebhook instances.
 * This abstraction allows for easier testing by enabling dependency injection and mocking.
 */
@ApplicationScoped
public class DiscordWebhookFactory {

    /**
     * Creates a new DiscordWebhook instance with the specified URL.
     *
     * @param url The webhook URL
     * @return A new DiscordWebhook instance
     */
    public DiscordWebhook create(String url) {
        return new DiscordWebhook(url);
    }
}
