package arn.roub.krabot.scrapper;

import arn.roub.krabot.exception.KralandScrapingException;
import jakarta.enterprise.context.ApplicationScoped;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieManager;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;

@ApplicationScoped
public class KralandScrappingClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KralandScrappingClient.class);
    private static final int MAX_PASSWORD_SIZE = 8;
    private static final String KRALAND_MAIL_ICON = "http://img.kraland.org/5/kmn.gif";
    private static final String MARK_AS_READ_ALT = "Marquer comme lu/non lu";
    
    private final HttpClient httpClient;
    private final HttpRequest loadKi;
    private final HttpRequest.Builder authKi;

    public KralandScrappingClient() {
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


            // Load the kramail page directly (authenticated content lives here)
            loadKi = HttpRequest.newBuilder(new URI("http://www.kraland.org/kramail")).GET().build();

            // Authentication now posts to /accueil with c[1]=login and c[2]=password
            authKi = HttpRequest.newBuilder(new URI("http://www.kraland.org/accueil"))
                    .headers("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36",
                            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                            "Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            "Accept-Encoding", "gzip, deflate",
                            "Content-Type", "application/x-www-form-urlencoded",
                            "Origin", "http://www.kraland.org",
                            "DNT", "1",
                            "Referer", "http://www.kraland.org/",
                            "Upgrade-Insecure-Requests", "1");
        } catch (URISyntaxException e) {
            throw new KralandScrapingException("Failed to initialize Kraland scraping client", e);
        }
    }


    public ScrappingResponse hasNotification(String kiUser, String kiPassword) {
        HttpResponse<String> response = null;
        HttpResponse<String> authResponse = null;
        Document document = null;
        Elements imgNodes = null;
        
        try {
            response = httpClient.send(loadKi, HttpResponse.BodyHandlers.ofString(StandardCharsets.ISO_8859_1));
            
            // Detect whether the page requires authentication (login form present)
            if (response.body().contains("Identifiant") || response.body().contains("S'identifier") || response.body().contains("c[1]")) {
                LOGGER.debug("Authentication required, logging in...");

                // Build form body: a=100&c[1]=username&c[2]=password&f[1]=1 (remember-me)
                String body = "a=100&c%5B1%5D=" + java.net.URLEncoder.encode(kiUser, java.nio.charset.StandardCharsets.ISO_8859_1)
                        + "&c%5B2%5D=" + java.net.URLEncoder.encode(kiPassword, java.nio.charset.StandardCharsets.ISO_8859_1)
                        + "&f%5B1%5D=1";

                // Send authentication request
                authResponse = httpClient.send(
                        authKi.POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.ISO_8859_1)
                );

                // Check auth response status
                if (authResponse.statusCode() >= 400) {
                    throw new KralandScrapingException(
                            "Authentication failed with status: " + authResponse.statusCode()
                    );
                }

                // Release auth response body to free memory
                authResponse = null;

                LOGGER.debug("Authentication successful, fetching notifications...");

                // Get authenticated page again
                response = httpClient.send(loadKi, HttpResponse.BodyHandlers.ofString(StandardCharsets.ISO_8859_1));
            }
            
            // Check final response status
            if (response.statusCode() >= 400) {
                throw new KralandScrapingException(
                    "Failed to fetch Kraland page with status: " + response.statusCode()
                );
            }
            
            String responseBody = response.body();

            // Parse HTML document using updated selectors and logic
            document = Jsoup.parse(responseBody);
            // Release response body reference to allow GC
            response = null;
            responseBody = null;

            // Use the new parsing logic based on badges and anchors
            ScrappingResponse parsed = parseKramailHtml(document);

            // Release document to allow GC
            document = null;

            return parsed;
        } catch (Exception e) {
            throw new KralandScrapingException("Failed to scrape Kraland notifications", e);
        } finally {
            // Ensure resources are released even on exception
            response = null;
            authResponse = null;
            document = null;
            imgNodes = null;
        }
    }

    // Visible helper to parse a raw Kramail HTML page (useful for Playwright flow and testing)
    public ScrappingResponse parseKramailHtml(String html) {
        Document doc = Jsoup.parse(html);
        return parseKramailDoc(doc);
    }

    private ScrappingResponse parseKramailHtml(Document doc) {
        return parseKramailDoc(doc);
    }

    private ScrappingResponse parseKramailDoc(Document doc) {
        boolean hasNotification = Optional.ofNullable(doc.selectFirst("span.badge.badge-danger"))
                .map(Element::text)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .isPresent();

        Elements anchors = doc.select("a[href*=\"kramail/post\"]");
        var kramails = new ArrayList<Kramail>();

        for (Element a : anchors) {
            String subject = Optional.ofNullable(a.text()).map(String::trim).orElse("");
            if (subject.isEmpty()) continue; // ignore icon-only anchors

            String href = a.attr("href");
            Element row = findAncestorWithTag(a, "tr");
            if (row == null) row = a.parent();

            String author = Optional.ofNullable(row.selectFirst("a[href*=\"communaute/membres\"]"))
                    .map(Element::text).map(String::trim).orElse("");

            kramails.add(new Kramail(href, subject, author));
        }

        return new ScrappingResponse(kramails, hasNotification);
    }

    private Element findAncestorWithTag(Element e, String tag) {
        Element parent = e.parent();
        while (parent != null) {
            if (parent.tagName().equalsIgnoreCase(tag)) return parent;
            parent = parent.parent();
        }
        return null;
    }
}
