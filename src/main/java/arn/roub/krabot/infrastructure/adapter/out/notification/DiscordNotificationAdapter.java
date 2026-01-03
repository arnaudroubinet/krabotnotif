package arn.roub.krabot.infrastructure.adapter.out.notification;

import arn.roub.krabot.domain.model.Kramail;
import arn.roub.krabot.domain.model.ReleaseVersion;
import arn.roub.krabot.domain.port.out.NotificationPort;
import arn.roub.krabot.shared.exception.RateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter Discord pour l'envoi de notifications.
 */
public class DiscordNotificationAdapter implements NotificationPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordNotificationAdapter.class);
    private static final String PLACEHOLDER_TITLE = "*title*";
    private static final String PLACEHOLDER_ORIGINATOR = "*originator*";
    private static final String PLACEHOLDER_RECIPIENT = "*recipient*";
    private static final String PLACEHOLDER_SECTION = "*section*";
    private static final String GITHUB_RELEASE_URL = "https://github.com/arnaudroubinet/krabotnotif/releases/latest";

    private final DiscordWebhookClient webhookClient;
    private final String startupMessage;
    private final String shutdownMessage;
    private final String kramailTemplate;
    private final String notificationMessage;
    private final String releaseTemplate;
    private final String errorPrefix;

    public DiscordNotificationAdapter(
            DiscordWebhookClient webhookClient,
            String startupMessage,
            String shutdownMessage,
            String kramailTemplate,
            String notificationMessage,
            String releaseTemplate,
            String errorPrefix
    ) {
        this.webhookClient = webhookClient;
        this.startupMessage = startupMessage;
        this.shutdownMessage = shutdownMessage;
        this.kramailTemplate = kramailTemplate;
        this.notificationMessage = notificationMessage;
        this.releaseTemplate = releaseTemplate;
        this.errorPrefix = errorPrefix;
    }

    @Override
    public void sendStartupNotification() {
        sendSafely(startupMessage, "startup");
    }

    @Override
    public void sendShutdownNotification() {
        sendSafely(shutdownMessage, "shutdown");
    }

    @Override
    public void sendKramailNotification(Kramail kramail) {
        String message = kramailTemplate
                .replace(PLACEHOLDER_TITLE, kramail.title())
                .replace(PLACEHOLDER_ORIGINATOR, kramail.originator())
                .replace(PLACEHOLDER_RECIPIENT, kramail.recipient())
                .replace(PLACEHOLDER_SECTION, kramail.section());

        sendWithRetry(message, "kramail");
    }

    @Override
    public void sendGeneralNotification() {
        sendWithRetry(notificationMessage, "general notification");
    }

    @Override
    public void sendReleaseNotification(ReleaseVersion version) {
        String message = releaseTemplate + ": " + GITHUB_RELEASE_URL;
        sendSafely(message, "release");
    }

    @Override
    public void sendErrorNotification(String message) {
        String fullMessage = errorPrefix + " " + message;
        sendSafely(fullMessage, "error");
    }

    private void sendWithRetry(String message, String type) {
        try {
            webhookClient.send(message);
            LOGGER.debug("Sent {} notification", type);
        } catch (RateLimitException e) {
            LOGGER.warn("Rate limited while sending {} notification, will retry later", type);
            throw e;
        }
    }

    private void sendSafely(String message, String type) {
        try {
            webhookClient.send(message);
            LOGGER.debug("Sent {} notification", type);
        } catch (RateLimitException e) {
            LOGGER.warn("Rate limited while sending {} notification", type);
        } catch (Exception e) {
            LOGGER.error("Failed to send {} notification: {}", type, e.getMessage());
        }
    }
}
