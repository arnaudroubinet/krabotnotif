package arn.roub.krabot.scrapper;

import arn.roub.krabot.exception.GithubApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieManager;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;

@ApplicationScoped
public class GithubScrappingClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubScrappingClient.class);
    private final HttpClient httpClient;
    private final HttpRequest latestReleaseRequest;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public GithubScrappingClient() {
        try {
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .cookieHandler(new CookieManager())
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .priority(1)
                    .proxy(ProxySelector.getDefault())
                    .version(HttpClient.Version.HTTP_2)
                    .executor(Executors.newVirtualThreadPerTaskExecutor()) // Java 21 virtual threads for better concurrency
                    .build();

            latestReleaseRequest = HttpRequest.newBuilder(new URI("https://api.github.com/repos/arnaudroubinet/krabotnotif/releases/latest")).GET().build();

        } catch (URISyntaxException e) {
            throw new GithubApiException("Failed to initialize GitHub API client", e);
        }
    }


    public String getLastReleaseTag() {
        HttpResponse<String> response = null;
        String responseBody = null;
        JsonNode jsonNode = null;
        
        try {
            response = httpClient.send(
                latestReleaseRequest, 
                HttpResponse.BodyHandlers.ofString()
            );
            
            int statusCode = response.statusCode();
            responseBody = response.body();
            
            // Handle rate limiting specially
            if (statusCode == 429) {
                String rateLimitReset = response.headers()
                    .firstValue("X-RateLimit-Reset")
                    .orElse("unknown");
                throw new GithubApiException(
                    String.format("GitHub API rate limit exceeded. Reset at: %s", rateLimitReset)
                );
            }
            
            // Handle other error responses
            if (statusCode != 200) {
                String errorMessage = String.format(
                    "GitHub API returned status %d: %s", 
                    statusCode,
                    responseBody.substring(0, Math.min(500, responseBody.length()))
                );
                throw new GithubApiException(errorMessage);
            }
            
            // Parse successful response
            jsonNode = OBJECT_MAPPER.readTree(responseBody);
            
            // Release response body early
            response = null;
            responseBody = null;
            
            JsonNode tagNode = jsonNode.get("tag_name");
            
            if (tagNode == null || tagNode.isNull()) {
                String jsonStr = jsonNode.toString();
                String preview = jsonStr.substring(0, Math.min(200, jsonStr.length()));
                // Release jsonNode before throwing
                jsonNode = null;
                throw new GithubApiException(
                    "GitHub API response missing 'tag_name' field. Response: " + preview
                );
            }
            
            String tag = tagNode.asText();
            
            // Release parsed JSON to allow GC
            jsonNode = null;
            
            LOGGER.debug("Retrieved latest GitHub release tag: {}", tag);
            return tag;
            
        } catch (GithubApiException e) {
            throw e;
        } catch (Exception e) {
            throw new GithubApiException("Failed to fetch latest release tag from GitHub", e);
        } finally {
            // Ensure all resources are released
            response = null;
            responseBody = null;
            jsonNode = null;
        }
    }
}
