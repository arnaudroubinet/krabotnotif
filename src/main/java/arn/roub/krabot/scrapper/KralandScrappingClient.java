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


            loadKi = HttpRequest.newBuilder(new URI("http://www.kraland.org/main.php?p=8_1")).GET().build();
            authKi = HttpRequest.newBuilder(new URI("http://www.kraland.org/main.php?p=1&a=100"))
                    .headers("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0",
                            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                            "Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            "Accept-Encoding", "gzip, deflate",
                            "Content-Type", "application/x-www-form-urlencoded",
                            "Origin", "http://www.kraland.org",
                            "DNT", "1",
                            "Referer", "http://www.kraland.org/main.php",
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
            
            if (response.body().contains("IDENTIFIER")) {
                LOGGER.debug("Authentication required, logging in...");
                String realPassword = kiPassword.length() > MAX_PASSWORD_SIZE ? kiPassword.substring(0, MAX_PASSWORD_SIZE) : kiPassword;
                String body = "p1=" + kiUser + "&p2=" + realPassword + "&Submit=Ok!";
                
                // Explicitly handle auth response
                authResponse = httpClient.send(
                    authKi.POST(HttpRequest.BodyPublishers.ofString(body)).build(), 
                    HttpResponse.BodyHandlers.ofString()
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
                
                // Get authenticated page
                response = httpClient.send(loadKi, HttpResponse.BodyHandlers.ofString());
            }
            
            // Check final response status
            if (response.statusCode() >= 400) {
                throw new KralandScrapingException(
                    "Failed to fetch Kraland page with status: " + response.statusCode()
                );
            }
            
            String responseBody = response.body();
            var report = responseBody.contains("report2.gif");
            var kramails = new ArrayList<Kramail>();

            // Parse HTML document
            document = Jsoup.parse(responseBody);
            // Release response body reference to allow GC
            response = null;
            responseBody = null;
            
            // Récupérer toutes les balises <img>
            imgNodes = document.select("img");

            imgNodes.forEach(element -> {
                if (KRALAND_MAIL_ICON.equals(element.attr("src")) && !MARK_AS_READ_ALT.equals(element.attr("alt"))) {
                    var parent = Optional.ofNullable(element.parent()).map(Element::parent).orElseThrow();
                    kramails.add(new Kramail(
                            parent.childNodes().get(2).childNode(0).attr("value"),
                            parent.childNodes().get(3).childNode(0).childNodes().stream()
                                    .filter(TextNode.class::isInstance)
                                    .reduce(Node::after)
                                    .orElseThrow()
                                    .outerHtml(),
                            parent.childNodes().get(4).childNode(0).outerHtml()));
                }
            });

            // Release document and nodes to allow GC
            imgNodes = null;
            document = null;

            return new ScrappingResponse(kramails, report);
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
}
