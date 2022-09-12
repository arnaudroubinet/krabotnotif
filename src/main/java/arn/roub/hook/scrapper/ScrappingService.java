package arn.roub.hook.scrapper;

import arn.roub.hook.utils.DiscordWebhook;
import arn.roub.hook.utils.PostponedNotificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ScrappingService {

    private final String hookUrl;
    private final String avatar;
    private final String username;
    private final String notificationMessage;
    private final String kramailMessage;
    private final String firstMessage;
    private final ScrappingClient scrappingClient;
    private final String kiUser;
    private final String kiPassword;

    private final AtomicBoolean reportNotificationIsAlreadySentFlag = new AtomicBoolean(false);
    private final AtomicBoolean kramailNotificationIsAlreadySentFlag = new AtomicBoolean(false);

    public ScrappingService(
            @Value("${discord.hook.url}") String hookUrl,
            @Value("${discord.hook.avatar.url}") String avatar,
            @Value("${discord.hook.username}") String username,
            @Value("${discord.hook.message.notification}") String notificationMessage,
            @Value("${discord.hook.message.kramail}") String kramailMessage,
            @Value("${discord.hook.firstMessage}") String firstMessage,
            ScrappingClient scrappingClient,
            @Value("${kraland.user}") String kiUser,
            @Value("${kraland.password}") String kiPassword) {
        this.hookUrl = hookUrl;
        this.avatar = avatar;
        this.username = username;
        this.notificationMessage = notificationMessage;
        this.kramailMessage = kramailMessage;
        this.firstMessage = firstMessage;
        this.scrappingClient = scrappingClient;
        this.kiUser = kiUser;
        this.kiPassword = kiPassword;

        initializeService();
    }

    public void loadKiAndSendNotificationIfWeHaveReport() {
        ScrappingResponse response = scrappingClient.hasNotification(kiUser, kiPassword);

        if (response.hasNotification()) {
            sendNotificationIfNotificationFlagIsTrue(notificationMessage, reportNotificationIsAlreadySentFlag);
        } else {
            reportNotificationIsAlreadySentFlag.set(false);
        }

        if (response.hasKramail()) {
            sendNotificationIfNotificationFlagIsTrue(kramailMessage, kramailNotificationIsAlreadySentFlag);
        } else {
            kramailNotificationIsAlreadySentFlag.set(false);
        }
    }


    private void sendNotificationIfNotificationFlagIsTrue(String message, AtomicBoolean flag) {
        try {
            if (!flag.get()) {
                flag.set(true);
                DiscordWebhook discordWebhook = new DiscordWebhook(hookUrl);
                discordWebhook.setAvatarUrl(avatar);
                discordWebhook.setUsername(username);
                discordWebhook.setContent(message);
                discordWebhook.setTts(false);
                discordWebhook.execute();
            }
        } catch (PostponedNotificationException ex) {
            flag.set(false);
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
        } catch (PostponedNotificationException ex) {
            //Do nothing
        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

}
