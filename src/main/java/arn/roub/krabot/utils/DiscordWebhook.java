package arn.roub.krabot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
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
 * Class used to execute Discord Webhooks with low effort
 */
public class DiscordWebhook {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordWebhook.class);
    private static final String X_RATE_LIMIT_LIMIT = "X-RateLimit-Limit";
    private static final String X_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String X_RATE_LIMIT_RESET = "X-RateLimit-Reset";
    private static final String X_RATE_LIMIT_RESET_AFTER = "X-RateLimit-Reset-After";
    private static final String X_RATE_LIMIT_BUCKET = "X-RateLimit-Bucket";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String url;
    private String content;
    private String username;
    private String avatarUrl;

    private OffsetDateTime resetAfter;
    private boolean tts;
    
    /**
     * Constructs a new DiscordWebhook instance
     *
     * @param url The webhook URL obtained in Discord
     */
    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setTts(boolean tts) {
        this.tts = tts;
    }

    public void execute() throws IOException {
        if (resetAfter != null && resetAfter.isAfter(OffsetDateTime.now())) {
            LOGGER.warn("Discord RateLimit reached and will be reset after {}. The notification is delayed.", resetAfter);
            throw new PostponedNotificationException();
        } else {
            resetAfter = null;
        }

        if (this.content == null) {
            throw new IllegalArgumentException("Content must be set before execution");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("content", this.content);
        payload.put("username", this.username);
        payload.put("avatar_url", this.avatarUrl);
        payload.put("tts", this.tts);

        URL url = URI.create(this.url).toURL();
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json;  charset=ISO-8859-1'");
        connection.addRequestProperty("User-Agent", "Kraland web hook");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(OBJECT_MAPPER.writeValueAsString(payload).getBytes(StandardCharsets.ISO_8859_1));
            stream.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200 && responseCode != 204)
            LOGGER.warn("Response code : {}", responseCode);
        else
            LOGGER.debug("Response code : {}", responseCode);

        Optional.ofNullable(connection.getHeaderField(X_RATE_LIMIT_REMAINING.toLowerCase()))
                .map(Long::valueOf)
                .ifPresent(remaining -> {
                    boolean isRateLimited = remaining <= 0;
                    
                    logRateLimitInfo(connection, remaining, isRateLimited);
                    
                    if (isRateLimited) {
                        resetAfter = OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(Long.parseLong(connection.getHeaderField(X_RATE_LIMIT_RESET.toLowerCase()))), 
                            ZoneOffset.systemDefault()
                        );
                        LOGGER.warn("Rate limit will be reset after: {}", resetAfter);
                    }
                });

        try (var inputStream = connection.getInputStream()) {
            inputStream.close();
        } finally {
            connection.disconnect();
        }
    }
    
    private void logRateLimitInfo(HttpsURLConnection connection, long remaining, boolean isRateLimited) {
        String limit = connection.getHeaderField(X_RATE_LIMIT_LIMIT.toLowerCase());
        String reset = connection.getHeaderField(X_RATE_LIMIT_RESET.toLowerCase());
        String resetAfterHeader = connection.getHeaderField(X_RATE_LIMIT_RESET_AFTER.toLowerCase());
        String bucket = connection.getHeaderField(X_RATE_LIMIT_BUCKET.toLowerCase());
        
        if (isRateLimited) {
            LOGGER.warn("{}: {}", X_RATE_LIMIT_LIMIT, limit);
            LOGGER.warn("{}: {}", X_RATE_LIMIT_REMAINING, remaining);
            LOGGER.warn("{}: {}", X_RATE_LIMIT_RESET, reset);
            LOGGER.warn("{}: {}", X_RATE_LIMIT_RESET_AFTER, resetAfterHeader);
            LOGGER.warn("{}: {}", X_RATE_LIMIT_BUCKET, bucket);
        } else {
            LOGGER.debug("{}: {}", X_RATE_LIMIT_LIMIT, limit);
            LOGGER.debug("{}: {}", X_RATE_LIMIT_REMAINING, remaining);
            LOGGER.debug("{}: {}", X_RATE_LIMIT_RESET, reset);
            LOGGER.debug("{}: {}", X_RATE_LIMIT_RESET_AFTER, resetAfterHeader);
            LOGGER.debug("{}: {}", X_RATE_LIMIT_BUCKET, bucket);
            LOGGER.debug("Rate limit will be reset after: {}", 
                OffsetDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(reset)), ZoneOffset.systemDefault()));
        }
    }
}
