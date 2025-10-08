package arn.roub.krabot.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Health check to verify Kraland website is reachable.
 */
@Readiness
@ApplicationScoped
public class KralandHealthCheck implements HealthCheck {

    private static final String KRALAND_URL = "http://www.kraland.org";

    @Override
    public HealthCheckResponse call() {
        try {
            URL url = URI.create(KRALAND_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return HealthCheckResponse.up("Kraland website is reachable");
            } else {
                return HealthCheckResponse.down("Kraland returned status: " + responseCode);
            }
        } catch (Exception e) {
            return HealthCheckResponse.down("Kraland unreachable: " + e.getMessage());
        }
    }
}
