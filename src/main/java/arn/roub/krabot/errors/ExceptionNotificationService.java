package arn.roub.krabot.errors;

import arn.roub.krabot.config.DiscordConfig;
import arn.roub.krabot.exception.DiscordNotificationException;
import arn.roub.krabot.utils.DiscordWebhook;
import arn.roub.krabot.utils.PostponedNotificationException;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ExceptionNotificationService {

    private final DiscordConfig discordConfig;

    public ExceptionNotificationService(DiscordConfig discordConfig) {
        this.discordConfig = discordConfig;
    }

    public void exceptionManagement(Throwable ex) {
        try {
            DiscordWebhook discordWebhook = new DiscordWebhook(discordConfig.url());
            discordWebhook.setAvatarUrl(discordConfig.avatarUrl());
            discordWebhook.setUsername(discordConfig.username());
            discordWebhook.setContent(discordConfig.errorPrefixMessage() +" "+ ex.getMessage());
            discordWebhook.setTts(false);
            discordWebhook.execute();

        } catch (PostponedNotificationException pnex) {
            //Do nothing
        } catch (Exception e) {
            throw new DiscordNotificationException("Failed to send error notification to Discord", e);
        }

    }

}
