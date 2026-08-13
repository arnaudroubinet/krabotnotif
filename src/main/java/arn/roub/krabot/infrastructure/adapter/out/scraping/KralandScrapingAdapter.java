package arn.roub.krabot.infrastructure.adapter.out.scraping;

import arn.roub.krabot.domain.model.Account;
import arn.roub.krabot.domain.model.Kramail;
import arn.roub.krabot.domain.model.ScrapingResult;
import arn.roub.krabot.domain.port.out.KralandScrapingPort;
import arn.roub.krabot.shared.exception.ScrapingException;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieManager;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Adapter pour le scraping de Kraland.
 */
@ApplicationScoped
public class KralandScrapingAdapter implements KralandScrapingPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(KralandScrapingAdapter.class);
    private static final String KRAMAIL_URL = "http://www.kraland.org/kramail";
    private static final String AUTH_URL = "http://www.kraland.org/accueil";
    private static final String PLATEAU_URL = "http://www.kraland.org/jouer/plateau";
    private static final String AJX_URL_BASE = "http://www.kraland.org/ajx/";

    private final HttpClient httpClient;
    private final KralandHtmlParser parser;

    public KralandScrapingAdapter() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .cookieHandler(new CookieManager())
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .priority(1)
                .proxy(ProxySelector.getDefault())
                .version(HttpClient.Version.HTTP_2)
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
        this.parser = new KralandHtmlParser();
    }

    @Override
    public ScrapingResult scrape(Account account) {
        try {
            HttpResponse<String> response = executeWithAuth(KRAMAIL_URL, account);
            String body = response.body();
            boolean hasNotification = parser.hasNotification(body);

            List<KralandHtmlParser.AccountInfo> accounts = parser.extractAccounts(body);
            if (accounts.isEmpty()) {
                LOGGER.warn("No kramail accounts found in sidebar. Page length: {}", body.length());
            }

            List<Kramail> allKramails = new ArrayList<>();

            for (KralandHtmlParser.AccountInfo accountInfo : accounts) {
                List<Kramail> kramails = scrapeAccountKramails(accountInfo, account);
                allKramails.addAll(kramails);
            }

            LOGGER.info("Found {} unread kramails across {} accounts", allKramails.size(), accounts.size());

            return new ScrapingResult(allKramails, hasNotification);
        } catch (ScrapingException e) {
            throw e;
        } catch (Exception e) {
            throw new ScrapingException("Failed to scrape Kraland notifications", e);
        }
    }

    private void performAuthentication(Account account) throws Exception {
        LOGGER.debug("Performing authentication...");

        String body = "a=100&c%5B1%5D=" + URLEncoder.encode(account.username(), StandardCharsets.ISO_8859_1)
                + "&c%5B2%5D=" + URLEncoder.encode(account.password(), StandardCharsets.ISO_8859_1)
                + "&f%5B1%5D=1";

        HttpRequest authRequest = HttpRequest.newBuilder(new URI(AUTH_URL))
                .headers(
                        "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
                        "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                        "Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                        "Content-Type", "application/x-www-form-urlencoded",
                        "Origin", "http://www.kraland.org",
                        "Referer", "http://www.kraland.org/"
                )
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> authResponse = httpClient.send(
                authRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.ISO_8859_1)
        );

        if (authResponse.statusCode() >= 400) {
            throw new ScrapingException("Authentication failed with status: " + authResponse.statusCode());
        }

        LOGGER.debug("Authentication successful");
    }

    /**
     * Exécute une requête GET et gère automatiquement la ré-authentification si la session a expiré.
     */
    private HttpResponse<String> executeWithAuth(String url, Account account) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(new URI(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() >= 400) {
            throw new ScrapingException("Request failed with status: " + response.statusCode());
        }

        // Si la session a expiré, on s'authentifie et on réessaie
        if (parser.requiresAuthentication(response.body())) {
            LOGGER.info("Session expired for {}, re-authenticating...", url);
            performAuthentication(account);

            response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 400) {
                throw new ScrapingException("Request failed after re-auth with status: " + response.statusCode());
            }

            if (parser.requiresAuthentication(response.body())) {
                throw new ScrapingException("Authentication failed: still on login page after re-auth for " + url);
            }
        }

        return response;
    }

    private List<Kramail> scrapeAccountKramails(KralandHtmlParser.AccountInfo accountInfo, Account account) throws Exception {
        HttpResponse<String> response = executeWithAuth(accountInfo.url(), account);

        List<Kramail> kramails = parser.parseKramails(response.body());
        LOGGER.debug("Account '{}': found {} unread kramails", accountInfo.name(), kramails.size());
        return kramails;
    }

    @Override
    public boolean sleepIfAvailable(Account account) {
        try {
            HttpResponse<String> plateauResponse = executeWithAuth(PLATEAU_URL, account);

            if (!parser.isSleepButtonAvailable(plateauResponse.body())) {
                LOGGER.debug("Sleep action not available (already done today)");
                return false;
            }

            KralandHtmlParser.AjaxOrderTrigger trigger = parser.findSleepAjaxTrigger(plateauResponse.body())
                    .orElseThrow(() -> new ScrapingException("Sleep button available but AJAX trigger not found"));

            String ajxUrl = AJX_URL_BASE + trigger.param() + "-" + Instant.now().toEpochMilli() + "/" + trigger.token();
            HttpResponse<String> orderFragment = executeWithAuth(ajxUrl, account);

            Map<String, String> formFields = parser.extractOrderFormFields(orderFragment.body());
            if (formFields.isEmpty()) {
                throw new ScrapingException("Sleep order form fields not found in AJAX fragment");
            }

            submitOrderForm(formFields);
            LOGGER.info("Sleep order submitted successfully");
            return true;
        } catch (ScrapingException e) {
            throw e;
        } catch (Exception e) {
            throw new ScrapingException("Failed to perform sleep action", e);
        }
    }

    private void submitOrderForm(Map<String, String> formFields) throws Exception {
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> field : formFields.entrySet()) {
            if (!body.isEmpty()) {
                body.append('&');
            }
            body.append(URLEncoder.encode(field.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(field.getValue(), StandardCharsets.UTF_8));
        }

        HttpRequest request = HttpRequest.newBuilder(new URI(PLATEAU_URL))
                .headers(
                        "Content-Type", "application/x-www-form-urlencoded",
                        "Origin", "http://www.kraland.org",
                        "Referer", PLATEAU_URL
                )
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() >= 400) {
            throw new ScrapingException("Sleep order submission failed with status: " + response.statusCode());
        }
    }
}
