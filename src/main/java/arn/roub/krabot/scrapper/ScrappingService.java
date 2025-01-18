package arn.roub.krabot.scrapper;

import arn.roub.krabot.utils.DiscordWebhook;
import arn.roub.krabot.utils.PostponedNotificationException;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import arn.roub.krabot.scrapper.CurrentState;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class ScrappingService {

    private final String hookUrl;
    private final String avatar;
    private final String username;
    private final String notificationMessage;
    private final String kramailMessage;
    private final String firstMessage;
    private final String lastMessage;
    private final ScrappingClient scrappingClient;
    private final String kiUser;
    private final String kiPassword;

    private final AtomicBoolean reportNotificationIsAlreadySentFlag = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, AtomicBoolean> kramailNotifAlreadySent = new ConcurrentHashMap<>();

    public ScrappingService(
            ScrappingClient scrappingClient,
            @ConfigProperty(name = "discord.hook.url") String hookUrl,
            @ConfigProperty(name = "discord.hook.avatar.url") String avatar,
            @ConfigProperty(name = "discord.hook.username") String username,
            @ConfigProperty(name = "discord.hook.message.notification") String notificationMessage,
            @ConfigProperty(name = "discord.hook.message.kramail") String kramailMessage,
            @ConfigProperty(name = "discord.hook.firstMessage") String firstMessage,
            @ConfigProperty(name = "discord.hook.lastMessage") String lastMessage,
            @ConfigProperty(name = "kraland.user") String kiUser,
            @ConfigProperty(name = "kraland.password") String kiPassword) {
        this.hookUrl = hookUrl;
        this.avatar = avatar;
        this.username = username;
        this.notificationMessage = notificationMessage;
        this.kramailMessage = kramailMessage;
        this.firstMessage = firstMessage;
        this.lastMessage = lastMessage;
        this.scrappingClient = scrappingClient;
        this.kiUser = kiUser;
        this.kiPassword = kiPassword;

        initializeService();
    }

    @PreDestroy
    void destroy() {
        sendNotificationIfNotificationFlagIsTrue(lastMessage, new AtomicBoolean(false));
    }

    public void loadKiAndSendNotificationIfWeHaveReport() {
        loadKiAndSendNotificationIfWeHaveReport(0);
    }

    public void loadKiAndSendNotificationIfWeHaveReport(int errorcounter) {
        try {
            ScrappingResponse response = scrappingClient.hasNotification(kiUser, kiPassword);

            if (response.hasNotification()) {
                sendNotificationIfNotificationFlagIsTrue(notificationMessage, reportNotificationIsAlreadySentFlag);
                CurrentState.hasNotification = true;
            } else {
                CurrentState.hasNotification = false;
                reportNotificationIsAlreadySentFlag.set(false);
            }

            if (!response.kramails().isEmpty()) {
                response.kramails().forEach(kramail -> {
                    AtomicBoolean isAlreadySent = kramailNotifAlreadySent.getOrDefault(kramail.id(),
                            new AtomicBoolean());
                    if (!isAlreadySent.get()) {
                        String message = kramailMessage
                                .replace("*title*", kramail.title())
                                .replace("*originator*", kramail.originator());

                        sendNotificationIfNotificationFlagIsTrue(message, isAlreadySent);
                    }
                    kramailNotifAlreadySent.put(kramail.id(), isAlreadySent);
                });

                List<String> currents = response.kramails().stream().map(Kramail::id).toList();
                kramailNotifAlreadySent.keySet()
                        .stream()
                        .filter(key -> !currents.contains(key))
                        .forEach(kramailNotifAlreadySent::remove);
            } else {
                kramailNotifAlreadySent.clear();
            }

            CurrentState.nbkramail = kramailNotifAlreadySent.size();
        } catch (RuntimeException ex) {
            if (errorcounter > 2) {
                throw ex;
            } else {
                loadKiAndSendNotificationIfWeHaveReport(errorcounter++);
            }
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
            // Do nothing
        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

}
