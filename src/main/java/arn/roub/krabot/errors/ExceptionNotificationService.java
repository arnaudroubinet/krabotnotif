package arn.roub.krabot.errors;

import arn.roub.krabot.config.DiscordConfig;
import arn.roub.krabot.exception.DiscordNotificationException;
import arn.roub.krabot.utils.DiscordWebhook;
import arn.roub.krabot.utils.DiscordWebhookFactory;
import arn.roub.krabot.utils.PostponedNotificationException;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ExceptionNotificationService {

    private final DiscordConfig discordConfig;
    private final DiscordWebhookFactory webhookFactory;

    public ExceptionNotificationService(DiscordConfig discordConfig, DiscordWebhookFactory webhookFactory) {
        this.discordConfig = discordConfig;
        this.webhookFactory = webhookFactory;
    }

    public void exceptionManagement(Throwable ex) {
        try {
            DiscordWebhook discordWebhook = webhookFactory.create(discordConfig.url());
            discordWebhook.setAvatarUrl(discordConfig.avatarUrl());
            discordWebhook.setUsername(discordConfig.username());
            discordWebhook.setContent("%s %s".formatted(discordConfig.errorPrefixMessage(), ex.getMessage()));
            discordWebhook.setTts(false);
            discordWebhook.execute();

        } catch (PostponedNotificationException ignoredException) {
            // Error notification postponed - acceptable as this is a best-effort notification
        } catch (Exception e) {
            throw new DiscordNotificationException("Failed to send error notification to Discord", e);
        }

    }

}
