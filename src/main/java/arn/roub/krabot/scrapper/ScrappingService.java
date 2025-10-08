package arn.roub.krabot.scrapper;

import arn.roub.krabot.config.DiscordConfig;
import arn.roub.krabot.exception.DiscordNotificationException;
import arn.roub.krabot.utils.DiscordWebhook;
import arn.roub.krabot.utils.DiscordWebhookFactory;
import arn.roub.krabot.utils.PostponedNotificationException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class ScrappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrappingService.class);
    private static final String PLACEHOLDER_TITLE = "*title*";
    private static final String PLACEHOLDER_ORIGINATOR = "*originator*";

    private final DiscordConfig discordConfig;
    private final KralandScrappingClient kralandScrappingClient;
    private final GithubScrappingClient githubScrappingClient;
    private final DiscordWebhookFactory webhookFactory;
    private final String kiUser;
    private final String kiPassword;
    private final AtomicBoolean reportNotificationIsAlreadySentFlag = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, AtomicBoolean> kramailNotifAlreadySent = new ConcurrentHashMap<>();
    private final CurrentState currentState;

    public ScrappingService(
            KralandScrappingClient kralandScrappingClient,
            CurrentState currentState,
            DiscordConfig discordConfig,
            GithubScrappingClient githubScrappingClient,
            DiscordWebhookFactory webhookFactory,
            @ConfigProperty(name = "kraland.user") String kiUser,
            @ConfigProperty(name = "kraland.password") String kiPassword) {
        this.discordConfig = discordConfig;
        this.kralandScrappingClient = kralandScrappingClient;
        this.githubScrappingClient = githubScrappingClient;
        this.webhookFactory = webhookFactory;
        this.kiUser = kiUser;
        this.kiPassword = kiPassword;
        this.currentState = currentState;
    }

    @PostConstruct
    void initialize() {
        try {
            this.currentState.setLatestVersion(githubScrappingClient.getLastReleaseTag());
            initializeService();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize service. GitHub release check or Discord notification failed.", e);
            // Application continues - initialization failure is not fatal
        }
    }

    @PreDestroy
    void destroy() {
        sendNotificationIfNotificationFlagIsTrue(discordConfig.lastMessage(), new AtomicBoolean(false));
    }

    public void loadKiAndSendNotificationIfWeHaveReport() {
        retryOnFailure(() -> {
            ScrappingResponse response = kralandScrappingClient.hasNotification(kiUser, kiPassword);

            if (response.hasNotification()) {
                sendNotificationIfNotificationFlagIsTrue(discordConfig.messageNotification(), reportNotificationIsAlreadySentFlag);
                currentState.setHasNotification(true);
            } else {
                currentState.setHasNotification(false);
                reportNotificationIsAlreadySentFlag.set(false);
            }

            if (!response.kramails().isEmpty()) {
                response.kramails().forEach(kramail -> {
                    AtomicBoolean isAlreadySent = kramailNotifAlreadySent.getOrDefault(kramail.id(),
                            new AtomicBoolean());
                    if (!isAlreadySent.get()) {
                        String message = discordConfig.messageKramail()
                                .replace(PLACEHOLDER_TITLE, kramail.title())
                                .replace(PLACEHOLDER_ORIGINATOR, kramail.originator());

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

            currentState.setNbkramail(kramailNotifAlreadySent.size());
        });
    }


    public void loadGithubAndSendNotificationIfWeHaveNewRelease() {
        retryOnFailure(() -> {
            String tag = githubScrappingClient.getLastReleaseTag();
            if(!tag.equals(currentState.getLatestVersion())) {
                currentState.setLatestVersion(tag);
                String message = discordConfig.release() + " : https://github.com/arnaudroubinet/krabotnotif/releases/latest";
                sendNotificationIfNotificationFlagIsTrue(message, new AtomicBoolean(false));
            }
        });
    }

    private void retryOnFailure(Runnable operation) {
        int maxRetries = 3;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                operation.run();
                return;
            } catch (RuntimeException ex) {
                if (attempt == maxRetries - 1) {
                    throw ex;
                }
            }
        }
    }

    private void sendNotificationIfNotificationFlagIsTrue(String message, AtomicBoolean flag) {
        try {
            if (!flag.get()) {
                flag.set(true);
                DiscordWebhook discordWebhook = webhookFactory.create(discordConfig.url());
                discordWebhook.setAvatarUrl(discordConfig.avatarUrl());
                discordWebhook.setUsername(discordConfig.username());
                discordWebhook.setContent(message);
                discordWebhook.setTts(false);
                discordWebhook.execute();
            }
        } catch (PostponedNotificationException ignoredException) {
            // Reset flag to allow retry - notification was postponed due to rate limiting
            flag.set(false);
        } catch (Exception e) {
            throw new DiscordNotificationException("Failed to send Discord notification", e);
        }
    }

    private void initializeService() {
        try {
            DiscordWebhook discordWebhook = webhookFactory.create(discordConfig.url());
            discordWebhook.setAvatarUrl(discordConfig.avatarUrl());
            discordWebhook.setUsername(discordConfig.username());
            discordWebhook.setContent(discordConfig.firstMessage());
            discordWebhook.setTts(false);
            discordWebhook.execute();
        } catch (PostponedNotificationException ignoredException) {
            // Notification postponed - this is acceptable during initialization
        } catch (Exception e) {
            throw new DiscordNotificationException("Failed to send initialization notification", e);
        }

    }

}
