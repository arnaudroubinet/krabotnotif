package arn.roub.krabot.infrastructure.adapter.out.github;

import arn.roub.krabot.domain.model.ReleaseVersion;
import arn.roub.krabot.shared.exception.GithubApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GithubReleaseAdapterTest {

    private HttpClient httpClient;
    private HttpResponse<String> response;
    private GithubReleaseAdapter adapter;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        httpClient = mock(HttpClient.class);
        response = mock(HttpResponse.class);
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(response);
        adapter = new GithubReleaseAdapter(httpClient, URI.create("https://api.github.test/releases/latest"));
    }

    @Test
    void returnsTagName() {
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"tag_name\":\"v3.6.0\",\"name\":\"Release v3.6.0\"}");

        assertEquals(ReleaseVersion.of("v3.6.0"), adapter.getLatestRelease());
    }

    @Test
    void rateLimited_throwsWithResetInfo() {
        when(response.statusCode()).thenReturn(429);
        when(response.body()).thenReturn("");
        when(response.headers()).thenReturn(
                HttpHeaders.of(Map.of("X-RateLimit-Reset", List.of("12345")), (a, b) -> true));

        GithubApiException ex = assertThrows(GithubApiException.class, adapter::getLatestRelease);
        assertTrue(ex.getMessage().contains("12345"));
    }

    @Test
    void nonOkStatus_throws() {
        when(response.statusCode()).thenReturn(500);
        when(response.body()).thenReturn("Internal error");

        GithubApiException ex = assertThrows(GithubApiException.class, adapter::getLatestRelease);
        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    void missingTagName_throws() {
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"name\":\"no tag\"}");

        assertThrows(GithubApiException.class, adapter::getLatestRelease);
    }

    @Test
    void ioFailure_isWrapped() throws Exception {
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenThrow(new IOException("network down"));

        GithubApiException ex = assertThrows(GithubApiException.class, adapter::getLatestRelease);
        assertInstanceOf(IOException.class, ex.getCause());
    }
}
