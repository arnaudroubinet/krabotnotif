package arn.roub.krabot.infrastructure.adapter.out.notification;

import arn.roub.krabot.shared.exception.NotificationException;
import arn.roub.krabot.shared.exception.RateLimitException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Client HTTP pour envoyer des messages via Discord Webhook.
 */
public class DiscordWebhookClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordWebhookClient.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String X_RATE_LIMIT_REMAINING = "x-ratelimit-remaining";
    private static final String X_RATE_LIMIT_RESET = "x-ratelimit-reset";

    private final String webhookUrl;
    private final String username;
    private final String avatarUrl;
    private OffsetDateTime resetAfter;

    public DiscordWebhookClient(String webhookUrl, String username, String avatarUrl) {
        this.webhookUrl = webhookUrl;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    /**
     * Envoie un message via le webhook Discord.
     *
     * @param content le contenu du message
     * @throws RateLimitException si le rate limit est atteint
     * @throws NotificationException si l'envoi échoue
     */
    public void send(String content) {
        checkRateLimit();

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content must not be null or blank");
        }

        HttpsURLConnection connection = null;
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", content);
            payload.put("username", username);
            payload.put("avatar_url", avatarUrl);
            payload.put("tts", false);

            URL url = URI.create(webhookUrl).toURL();
            connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.addRequestProperty("User-Agent", "Krabot Webhook");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(OBJECT_MAPPER.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8));
                stream.flush();
            }

            int responseCode = connection.getResponseCode();
            handleRateLimitHeaders(connection);

            if (responseCode != 200 && responseCode != 204) {
                LOGGER.warn("Discord webhook returned status: {}", responseCode);
            }

            consumeResponse(connection, responseCode);

        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new NotificationException("Failed to send Discord notification", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void checkRateLimit() {
        if (resetAfter != null && resetAfter.isAfter(OffsetDateTime.now())) {
            LOGGER.warn("Discord rate limit active until {}", resetAfter);
            throw new RateLimitException("Rate limit active until " + resetAfter);
        }
        resetAfter = null;
    }

    private void handleRateLimitHeaders(HttpsURLConnection connection) {
        Optional.ofNullable(connection.getHeaderField(X_RATE_LIMIT_REMAINING))
                .map(Long::valueOf)
                .ifPresent(remaining -> {
                    if (remaining <= 0) {
                        String resetHeader = connection.getHeaderField(X_RATE_LIMIT_RESET);
                        if (resetHeader != null) {
                            resetAfter = OffsetDateTime.ofInstant(
                                    Instant.ofEpochSecond(Long.parseLong(resetHeader)),
                                    ZoneOffset.systemDefault()
                            );
                            LOGGER.warn("Rate limit will be reset after: {}", resetAfter);
                        }
                    }
                });
    }

    private void consumeResponse(HttpsURLConnection connection, int responseCode) throws Exception {
        try (InputStream stream = (responseCode >= 200 && responseCode < 300)
                ? connection.getInputStream()
                : connection.getErrorStream()) {
            if (stream != null) {
                stream.transferTo(OutputStream.nullOutputStream());
            }
        }
    }
}
