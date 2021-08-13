package arn.roub.hook.scrapper;

import arn.roub.hook.utils.DiscordWebhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ScrappingService {

    private final String hookUrl;
    private final String avatar;
    private final String username;
    private final String message;
    private final String firstMessage;
    private final ScrappingClient scrappingClient;

    private boolean notificationIsAlreadySent = false;

    public ScrappingService(
            @Value("${discord.hook.url}") String hookUrl,
            @Value("${discord.hook.avatar.url}") String avatar,
            @Value("${discord.hook.username}") String username,
            @Value("${discord.hook.message}") String message,
            @Value("${discord.hook.firstMessage}") String firstMessage,
            ScrappingClient scrappingClient) {
        this.hookUrl = hookUrl;
        this.avatar = avatar;
        this.username = username;
        this.message = message;
        this.firstMessage = firstMessage;
        this.scrappingClient = scrappingClient;

        initializeService();
    }

    public void loadKiAndSendNotificationIfWeHaveReport() {
        if (scrappingClient.hasNotification()) {
            sendNotificationIfNotificationFlagIsTrue();
        } else {
            setNotificationFlag(false);
        }
    }

    private void setNotificationFlag(boolean b) {
        notificationIsAlreadySent = b;
    }

    private void sendNotificationIfNotificationFlagIsTrue() {
        try {
            if (!notificationIsAlreadySent) {
                setNotificationFlag(true);
                DiscordWebhook discordWebhook = new DiscordWebhook(hookUrl);
                discordWebhook.setAvatarUrl(avatar);
                discordWebhook.setUsername(username);
                discordWebhook.setContent(message);
                discordWebhook.setTts(false);
                discordWebhook.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeService() {
        try {
            DiscordWebhook discordWebhook = new DiscordWebhook(hookUrl);
            discordWebhook.setAvatarUrl(avatar);
            discordWebhook.setUsername(username);
            discordWebhook.setContent(firstMessage);
            discordWebhook.setTts(false);
            discordWebhook.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

}
