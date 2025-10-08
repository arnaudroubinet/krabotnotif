package arn.roub.krabot.scrapper;

import arn.roub.krabot.exception.GithubApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.CookieManager;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class GithubScrappingClient {

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
                    .build();

            latestReleaseRequest = HttpRequest.newBuilder(new URI("https://api.github.com/repos/arnaudroubinet/krabotnotif/releases/latest")).GET().build();

        } catch (URISyntaxException e) {
            throw new GithubApiException("Failed to initialize GitHub API client", e);
        }
    }


    public String getLastReleaseTag() {
        try {
            HttpResponse<String> response = httpClient.send(latestReleaseRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonNode = OBJECT_MAPPER.readTree(response.body());
            return jsonNode.get("tag_name").asText();
        } catch (Exception e) {
            throw new GithubApiException("Failed to fetch latest release tag from GitHub", e);
        }
    }
}
