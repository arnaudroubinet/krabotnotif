package arn.roub.krabot.utils;

import arn.roub.krabot.utils.elements.Author;
import arn.roub.krabot.utils.elements.EmbedObject;
import arn.roub.krabot.utils.elements.Field;
import arn.roub.krabot.utils.elements.Footer;
import arn.roub.krabot.utils.elements.Image;
import arn.roub.krabot.utils.elements.Thumbnail;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final List<EmbedObject> embeds = new ArrayList<>();

    /**
     * Constructs a new DiscordWebhook instance
     *
     * @param url The webhook URL obtained in Discord
     */
    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
    }

    public void execute() throws IOException {

        if (resetAfter != null && resetAfter.isAfter(OffsetDateTime.now())) {
            LOGGER.warn("Discord RateLimit reach and will be reset after {}. The notification sent is delay.", resetAfter);
            throw new PostponedNotificationException();
        } else {
            resetAfter = null;
        }

        if (this.content == null && this.embeds.isEmpty()) {
            throw new IllegalArgumentException("Set content or add at least one EmbedObject");
        }

        JSONObject json = new JSONObject();

        json.put("content", this.content);
        json.put("username", this.username);
        json.put("avatar_url", this.avatarUrl);
        json.put("tts", this.tts);

        if (!this.embeds.isEmpty()) {
            List<JSONObject> embedObjects = new ArrayList<>();

            for (EmbedObject embed : this.embeds) {
                JSONObject jsonEmbed = new JSONObject();

                jsonEmbed.put("title", embed.getTitle());
                jsonEmbed.put("description", embed.getDescription());
                jsonEmbed.put("url", embed.getUrl());

                if (embed.getColor() != null) {
                    Color color = embed.getColor();
                    int rgb = color.getRed();
                    rgb = (rgb << 8) + color.getGreen();
                    rgb = (rgb << 8) + color.getBlue();

                    jsonEmbed.put("color", rgb);
                }

                Footer footer = embed.getFooter();
                Image image = embed.getImage();
                Thumbnail thumbnail = embed.getThumbnail();
                Author author = embed.getAuthor();
                List<Field> fields = embed.getFields();

                if (footer != null) {
                    JSONObject jsonFooter = new JSONObject();

                    jsonFooter.put("text", footer.getText());
                    jsonFooter.put("icon_url", footer.getIconUrl());
                    jsonEmbed.put("footer", jsonFooter);
                }

                if (image != null) {
                    JSONObject jsonImage = new JSONObject();

                    jsonImage.put("url", image.getUrl());
                    jsonEmbed.put("image", jsonImage);
                }

                if (thumbnail != null) {
                    JSONObject jsonThumbnail = new JSONObject();

                    jsonThumbnail.put("url", thumbnail.getUrl());
                    jsonEmbed.put("thumbnail", jsonThumbnail);
                }

                if (author != null) {
                    JSONObject jsonAuthor = new JSONObject();

                    jsonAuthor.put("name", author.getName());
                    jsonAuthor.put("url", author.getIconUrl());
                    jsonAuthor.put("icon_url", author.getIconUrl());
                    jsonEmbed.put("author", jsonAuthor);
                }

                List<JSONObject> jsonFields = new ArrayList<>();
                for (Field field : fields) {
                    JSONObject jsonField = new JSONObject();

                    jsonField.put("name", field.getName());
                    jsonField.put("value", field.getValue());
                    jsonField.put("inline", field.getValue());

                    jsonFields.add(jsonField);
                }

                jsonEmbed.put("fields", jsonFields.toArray());
                embedObjects.add(jsonEmbed);
            }

            json.put("embeds", embedObjects.toArray());
        }

        URL url = URI.create(this.url).toURL();
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "Kraland web hook");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        OutputStream stream = connection.getOutputStream();
        stream.write(json.toString().getBytes());
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
