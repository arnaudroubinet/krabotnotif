package arn.roub.krabot.infrastructure.adapter.out.scraping;

import arn.roub.krabot.domain.model.Account;
import arn.roub.krabot.domain.model.Kramail;
import arn.roub.krabot.domain.model.KramailId;
import arn.roub.krabot.domain.model.ScrapingResult;
import arn.roub.krabot.shared.exception.ScrapingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class KralandScrapingAdapterTest {

    private static final Account ACCOUNT = new Account("user", "secret");

    private HttpClient httpClient;
    private KralandHtmlParser parser;
    private HttpResponse<String> response;
    private KralandScrapingAdapter adapter;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        httpClient = mock(HttpClient.class);
        parser = mock(KralandHtmlParser.class);
        response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("<html/>");
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(response);
        adapter = new KralandScrapingAdapter(httpClient, parser);
    }

    private List<HttpRequest> sentRequests() throws Exception {
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, atLeastOnce()).send(captor.capture(), any());
        return captor.getAllValues();
    }

    @Test
    void scrape_aggregatesKramailsOfAllAccounts() throws Exception {
        Kramail k1 = new Kramail(new KramailId("1"), "t1", "a", "Bob");
        Kramail k2 = new Kramail(new KramailId("2"), "t2", "b", "Alice");
        when(parser.hasNotification(anyString())).thenReturn(true);
        when(parser.extractAccounts(anyString())).thenReturn(List.of(
                new KralandHtmlParser.AccountInfo("Bob", "http://www.kraland.org/kramail/bob"),
                new KralandHtmlParser.AccountInfo("Alice", "http://www.kraland.org/kramail/alice")));
        when(parser.parseKramails(anyString())).thenReturn(List.of(k1), List.of(k2));

        ScrapingResult result = adapter.scrape(ACCOUNT);

        assertTrue(result.hasNotification());
        assertEquals(List.of(k1, k2), result.kramails());
        List<String> urls = sentRequests().stream().map(r -> r.uri().toString()).toList();
        assertEquals(List.of(
                "http://www.kraland.org/kramail",
                "http://www.kraland.org/kramail/bob",
                "http://www.kraland.org/kramail/alice"), urls);
    }

    @Test
    void scrape_reauthenticatesWhenSessionExpired() throws Exception {
        when(parser.requiresAuthentication(anyString())).thenReturn(true, false);
        when(parser.extractAccounts(anyString())).thenReturn(List.of());

        adapter.scrape(ACCOUNT);

        List<HttpRequest> requests = sentRequests();
        assertEquals(3, requests.size(), "GET, POST auth, GET retry");
        assertEquals("POST", requests.get(1).method());
        assertEquals("http://www.kraland.org/accueil", requests.get(1).uri().toString());
        assertEquals("GET", requests.get(2).method());
    }

    @Test
    void scrape_failsWhenStillOnLoginPageAfterReauth() {
        when(parser.requiresAuthentication(anyString())).thenReturn(true);

        assertThrows(ScrapingException.class, () -> adapter.scrape(ACCOUNT));
    }

    @Test
    void scrape_failsOnHttpError() {
        when(response.statusCode()).thenReturn(503);

        ScrapingException ex = assertThrows(ScrapingException.class, () -> adapter.scrape(ACCOUNT));
        assertTrue(ex.getMessage().contains("503"));
    }

    @Test
    void scrape_wrapsIoErrors() throws Exception {
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenThrow(new IOException("down"));

        ScrapingException ex = assertThrows(ScrapingException.class, () -> adapter.scrape(ACCOUNT));
        assertInstanceOf(IOException.class, ex.getCause());
    }

    @Test
    void sleep_returnsFalseWhenButtonNotAvailable() throws Exception {
        when(parser.isSleepButtonAvailable(anyString())).thenReturn(false);

        assertFalse(adapter.sleepIfAvailable(ACCOUNT));
        assertEquals(1, sentRequests().size());
    }

    @Test
    void sleep_submitsOrderForm() throws Exception {
        when(parser.isSleepButtonAvailable(anyString())).thenReturn(true);
        when(parser.findSleepAjaxTrigger(anyString()))
                .thenReturn(Optional.of(new KralandHtmlParser.AjaxOrderTrigger("p1", "tok")));
        when(parser.extractOrderFormFields(anyString())).thenReturn(Map.of("a", "1"));

        assertTrue(adapter.sleepIfAvailable(ACCOUNT));

        List<HttpRequest> requests = sentRequests();
        assertEquals(3, requests.size(), "GET plateau, GET ajax fragment, POST order");
        assertTrue(requests.get(1).uri().toString().startsWith("http://www.kraland.org/ajx/p1-"));
        assertTrue(requests.get(1).uri().toString().endsWith("/tok"));
        assertEquals("POST", requests.get(2).method());
        assertEquals("http://www.kraland.org/jouer/plateau", requests.get(2).uri().toString());
    }

    @Test
    void sleep_failsWhenTriggerMissing() {
        when(parser.isSleepButtonAvailable(anyString())).thenReturn(true);
        when(parser.findSleepAjaxTrigger(anyString())).thenReturn(Optional.empty());

        assertThrows(ScrapingException.class, () -> adapter.sleepIfAvailable(ACCOUNT));
    }

    @Test
    void sleep_failsWhenFormFieldsMissing() {
        when(parser.isSleepButtonAvailable(anyString())).thenReturn(true);
        when(parser.findSleepAjaxTrigger(anyString()))
                .thenReturn(Optional.of(new KralandHtmlParser.AjaxOrderTrigger("p1", "tok")));
        when(parser.extractOrderFormFields(anyString())).thenReturn(Map.of());

        assertThrows(ScrapingException.class, () -> adapter.sleepIfAvailable(ACCOUNT));
    }
}
