package arn.roub.krabot.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Health check to verify GitHub API is reachable.
 */
@Readiness
@ApplicationScoped
public class GithubApiHealthCheck implements HealthCheck {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/arnaudroubinet/krabotnotif/releases/latest";

    @Override
    public HealthCheckResponse call() {
        try {
            URL url = URI.create(GITHUB_API_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return HealthCheckResponse.up("GitHub API is reachable");
            } else {
                return HealthCheckResponse.down("GitHub API returned status: " + responseCode);
            }
        } catch (Exception e) {
            return HealthCheckResponse.down("GitHub API unreachable: " + e.getMessage());
        }
    }
}
