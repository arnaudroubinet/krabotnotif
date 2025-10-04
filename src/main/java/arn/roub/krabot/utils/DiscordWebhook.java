package arn.roub.krabot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Class used to execute Discord Webhooks with low effort
 */
public class DiscordWebhook {
    private static final String X_RATE_LIMIT_LIMIT = "X-RateLimit-Limit";
    private static final String X_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String X_RATE_LIMIT_RESET = "X-RateLimit-Reset";
    private static final String X_RATE_LIMIT_RESET_AFTER = "X-RateLimit-Reset-After";
    private static final String X_RATE_LIMIT_BUCKET = "X-RateLimit-Bucket";

    private final Logger LOGGER = LoggerFactory.getLogger(DiscordWebhook.class);
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
            LOGGER.warn("Discord RateLimit reach and will be reset after {}. The notification sent is delay.", resetAfter);
            throw new PostponedNotificationException();
        } else {
            resetAfter = null;
        }

        if (this.content == null) {
            throw new IllegalArgumentException("Content must be set");
        }

        JSONObject json = new JSONObject();

        json.put("content", this.content);
        json.put("username", this.username);
        json.put("avatar_url", this.avatarUrl);
        json.put("tts", this.tts);

        URL url = URI.create(this.url).toURL();
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.addRequestProperty("User-Agent", "Kraland web hook");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        OutputStream stream = connection.getOutputStream();
        stream.write(json.toString().getBytes(StandardCharsets.UTF_8));
        stream.flush();
        stream.close();

        int responseCode = connection.getResponseCode();
        if (responseCode != 200 && responseCode != 204)
            LOGGER.warn("Response code : {}", responseCode);
        else
            LOGGER.debug("Response code : {}", responseCode);

        Optional.ofNullable(connection.getHeaderField(X_RATE_LIMIT_REMAINING.toLowerCase()))
                .map(Long::valueOf)
                .ifPresent(remaining -> {
                            String limit = connection.getHeaderField(X_RATE_LIMIT_LIMIT.toLowerCase());
                            String reset = connection.getHeaderField(X_RATE_LIMIT_RESET.toLowerCase());
                            String resetAfterHeader = connection.getHeaderField(X_RATE_LIMIT_RESET_AFTER.toLowerCase());
                            String bucket = connection.getHeaderField(X_RATE_LIMIT_BUCKET.toLowerCase());
                            
                            if (remaining <= 0) {
                                LOGGER.warn("{} : {}", X_RATE_LIMIT_LIMIT, limit);
                                LOGGER.warn("{} : {}", X_RATE_LIMIT_REMAINING, remaining);
                                LOGGER.warn("{} : {}", X_RATE_LIMIT_RESET, reset);
                                LOGGER.warn("{} : {}", X_RATE_LIMIT_RESET_AFTER, resetAfterHeader);
                                LOGGER.warn("{} : {}", X_RATE_LIMIT_BUCKET, bucket);
                                resetAfter = OffsetDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(reset)), ZoneOffset.systemDefault());
                                LOGGER.warn("Rate limit will be reset after : {}", resetAfter);
                            } else {
                                LOGGER.debug("{} : {}", X_RATE_LIMIT_LIMIT, limit);
                                LOGGER.debug("{} : {}", X_RATE_LIMIT_REMAINING, remaining);
                                LOGGER.debug("{} : {}", X_RATE_LIMIT_RESET, reset);
                                LOGGER.debug("{} : {}", X_RATE_LIMIT_RESET_AFTER, resetAfterHeader);
                                LOGGER.debug("{} : {}", X_RATE_LIMIT_BUCKET, bucket);
                                OffsetDateTime resetTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(reset)), ZoneOffset.systemDefault());
                                LOGGER.debug("Rate limit will be reset after : {}", resetTime);
                            }
                        }
                );

        connection.getInputStream().close();
        connection.disconnect();
    }



    private static class JSONObject {

        private final HashMap<String, Object> map = new HashMap<>();

        void put(String key, Object value) {
            if (value != null) {
                map.put(key, value);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            Set<Map.Entry<String, Object>> entrySet = map.entrySet();
            builder.append("{");

            int i = 0;
            for (Map.Entry<String, Object> entry : entrySet) {
                Object val = entry.getValue();
                builder.append(quote(entry.getKey())).append(":");

                if (val instanceof String) {
                    builder.append(quote(String.valueOf(val)));
                } else if (val instanceof Integer) {
                    builder.append(val);
                } else if (val instanceof Boolean) {
                    builder.append(val);
                } else if (val instanceof JSONObject) {
                    builder.append(val);
                } else if (val.getClass().isArray()) {
                    builder.append("[");
                    int len = Array.getLength(val);
                    for (int j = 0; j < len; j++) {
                        builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
                    }
                    builder.append("]");
                }

                builder.append(++i == entrySet.size() ? "}" : ",");
            }

            return builder.toString();
        }

        private String quote(String string) {
            return "\"" + string + "\"";
        }
    }

}
