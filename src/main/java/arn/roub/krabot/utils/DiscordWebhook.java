package arn.roub.krabot.utils;

import lombok.Setter;
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
    @Setter
    private String content;
    @Setter
    private String username;
    @Setter
    private String avatarUrl;

    private OffsetDateTime resetAfter;
    @Setter
    private boolean tts;
    /**
     * Constructs a new DiscordWebhook instance
     *
     * @param url The webhook URL obtained in Discord
     */
    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void execute() throws IOException {

        if (resetAfter != null && resetAfter.isAfter(OffsetDateTime.now())) {
            LOGGER.warn("Discord RateLimit reach and will be reset after {}. The notification sent is delay.", resetAfter);
            throw new PostponedNotificationException();
        } else {
            resetAfter = null;
        }

        if (this.content == null) {
            throw new IllegalArgumentException("Set content or add at least one EmbedObject");
        }

        JSONObject json = new JSONObject();

        json.put("content", this.content);
        json.put("username", this.username);
        json.put("avatar_url", this.avatarUrl);
        json.put("tts", this.tts);

        URL url = URI.create(this.url).toURL();
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json;  charset=ISO-8859-1'");
        connection.addRequestProperty("User-Agent", "Kraland web hook");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        OutputStream stream = connection.getOutputStream();
        stream.write(json.toString().getBytes(StandardCharsets.ISO_8859_1));
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
                            if (remaining <= 0) {
                                LOGGER.warn("{} : {}", X_RATE_LIMIT_LIMIT, connection.getHeaderField(X_RATE_LIMIT_LIMIT.toLowerCase()));
                                LOGGER.warn("{} : {}", X_RATE_LIMIT_REMAINING, remaining);
                                LOGGER.warn("{} : {}", X_RATE_LIMIT_RESET, connection.getHeaderField(X_RATE_LIMIT_RESET.toLowerCase()));
                                LOGGER.warn("{} : {}", X_RATE_LIMIT_RESET_AFTER, connection.getHeaderField(X_RATE_LIMIT_RESET_AFTER.toLowerCase()));
                                LOGGER.warn("{} : {}", X_RATE_LIMIT_BUCKET, connection.getHeaderField(X_RATE_LIMIT_BUCKET.toLowerCase()));
                                resetAfter = OffsetDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(connection.getHeaderField(X_RATE_LIMIT_RESET.toLowerCase()))), ZoneOffset.systemDefault());
                                LOGGER.warn("Rate limit will be reset after : {}", resetAfter);
                            } else {
                                LOGGER.debug("{} : {}", X_RATE_LIMIT_LIMIT, connection.getHeaderField(X_RATE_LIMIT_LIMIT.toLowerCase()));
                                LOGGER.debug("{} : {}", X_RATE_LIMIT_REMAINING, remaining);
                                LOGGER.debug("{} : {}", X_RATE_LIMIT_RESET, connection.getHeaderField(X_RATE_LIMIT_RESET.toLowerCase()));
                                LOGGER.debug("{} : {}", X_RATE_LIMIT_RESET_AFTER, connection.getHeaderField(X_RATE_LIMIT_RESET_AFTER.toLowerCase()));
                                LOGGER.debug("{} : {}", X_RATE_LIMIT_BUCKET, connection.getHeaderField(X_RATE_LIMIT_BUCKET.toLowerCase()));
                                LOGGER.debug("Rate limit will be reset after : {}", OffsetDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(connection.getHeaderField(X_RATE_LIMIT_RESET.toLowerCase()))), ZoneOffset.systemDefault()));
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
                    builder.append(Integer.valueOf(String.valueOf(val)));
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
