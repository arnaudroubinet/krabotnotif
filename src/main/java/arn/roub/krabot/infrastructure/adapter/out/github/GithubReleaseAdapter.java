package arn.roub.krabot.infrastructure.adapter.out.github;

import arn.roub.krabot.domain.model.ReleaseVersion;
import arn.roub.krabot.domain.port.out.GithubReleasePort;
import arn.roub.krabot.shared.exception.GithubApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieManager;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * Adapter pour récupérer les releases depuis GitHub.
 */
@ApplicationScoped
public class GithubReleaseAdapter implements GithubReleasePort {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubReleaseAdapter.class);
    private static final String GITHUB_API_URL = "https://api.github.com/repos/arnaudroubinet/krabotnotif/releases/latest";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final HttpRequest latestReleaseRequest;

    public GithubReleaseAdapter() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .cookieHandler(new CookieManager())
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .priority(1)
                .proxy(ProxySelector.getDefault())
                .version(HttpClient.Version.HTTP_2)
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();

        try {
            this.latestReleaseRequest = HttpRequest.newBuilder(new URI(GITHUB_API_URL)).GET().build();
        } catch (Exception e) {
            throw new GithubApiException("Failed to initialize GitHub API client", e);
        }
    }

    @Override
    public ReleaseVersion getLatestRelease() {
        try {
            HttpResponse<String> response = httpClient.send(
                    latestReleaseRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode == 429) {
                String rateLimitReset = response.headers()
                        .firstValue("X-RateLimit-Reset")
                        .orElse("unknown");
                throw new GithubApiException(
                        String.format("GitHub API rate limit exceeded. Reset at: %s", rateLimitReset)
                );
            }

            if (statusCode != 200) {
                throw new GithubApiException(String.format(
                        "GitHub API returned status %d: %s",
                        statusCode,
                        responseBody.substring(0, Math.min(500, responseBody.length()))
                ));
            }

            JsonNode jsonNode = OBJECT_MAPPER.readTree(responseBody);
            JsonNode tagNode = jsonNode.get("tag_name");

            if (tagNode == null || tagNode.isNull()) {
                throw new GithubApiException("GitHub API response missing 'tag_name' field");
            }

            String tag = tagNode.asText();
            LOGGER.debug("Retrieved latest GitHub release tag: {}", tag);

            return ReleaseVersion.of(tag);

        } catch (GithubApiException e) {
            throw e;
        } catch (Exception e) {
            throw new GithubApiException("Failed to fetch latest release tag from GitHub", e);
        }
    }
}
