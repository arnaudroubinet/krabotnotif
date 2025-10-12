package arn.roub.krabot.health;

import arn.roub.krabot.config.DiscordConfig;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Health check to verify Discord webhook is reachable.
 */
@Readiness
@ApplicationScoped
public class DiscordWebhookHealthCheck implements HealthCheck {

    private final DiscordConfig discordConfig;

    public DiscordWebhookHealthCheck(DiscordConfig discordConfig) {
        this.discordConfig = discordConfig;
    }

    @Override
    public HealthCheckResponse call() {
        HttpsURLConnection connection = null;
        try {
            URL url = URI.create(discordConfig.url()).toURL();
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            
            // Consume response stream to free resources (even for HEAD requests)
            try (InputStream stream = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream()) {
                if (stream != null) {
                    stream.transferTo(OutputStream.nullOutputStream());
                }
            }
            
            return switch (responseCode) {
                case HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NO_CONTENT -> 
                    HealthCheckResponse.up("Discord webhook is reachable");
                default -> HealthCheckResponse.down("Discord webhook returned status: " + responseCode);
            };
        } catch (Exception e) {
            return HealthCheckResponse.down("Discord webhook unreachable: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
