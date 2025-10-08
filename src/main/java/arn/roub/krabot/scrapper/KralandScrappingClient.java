package arn.roub.krabot.scrapper;

import arn.roub.krabot.exception.KralandScrapingException;
import jakarta.enterprise.context.ApplicationScoped;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

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

@ApplicationScoped
public class KralandScrappingClient {

    private final HttpClient httpClient;
    private final HttpRequest loadKi;
    private final HttpRequest.Builder authKi;

    private final static int MAX_PASSWORD_SIZE = 8;

    public KralandScrappingClient() {
        try {
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .cookieHandler(new CookieManager())
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .priority(1)
                    .proxy(ProxySelector.getDefault())
                    .version(HttpClient.Version.HTTP_2)
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
        try {
            HttpResponse<String> response = httpClient.send(loadKi, HttpResponse.BodyHandlers.ofString(StandardCharsets.ISO_8859_1));
            if (response.body().contains("IDENTIFIER")) {
                String realPassword = kiPassword.length() > MAX_PASSWORD_SIZE ? kiPassword.substring(0, MAX_PASSWORD_SIZE) : kiPassword;
                String body = "p1=" + kiUser + "&p2=" + realPassword + "&Submit=Ok!";
                httpClient.send(authKi.POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
                response = httpClient.send(loadKi, HttpResponse.BodyHandlers.ofString());
            }
            var report = response.body().contains("report2.gif");
            var kramails = new ArrayList<Kramail>();

            // Convertir la chaîne HTML en InputStream);
            Document document = Jsoup.parse(response.body());
            // Récupérer toutes les balises <img>
            Elements imgNodes = document.select("img");

            imgNodes.forEach(element -> {
                if ("http://img.kraland.org/5/kmn.gif".equals(element.attr("src")) && !"Marquer comme lu/non lu".equals(element.attr("alt"))) {
                    var parent = Optional.ofNullable(element.parent()).map(Element::parent).orElseThrow();
                    kramails.add(new Kramail(
                            parent.childNodes().get(2).childNode(0).attr("value"),
                            parent.childNodes().get(3).childNode(0).childNodes().stream().filter(node -> TextNode.class.isAssignableFrom(node.getClass())).reduce(Node::after).orElseThrow().outerHtml(),
                            parent.childNodes().get(4).childNode(0).outerHtml()));
                }
            });


            return new ScrappingResponse(kramails, report);
        } catch (Exception e) {
            throw new KralandScrapingException("Failed to scrape Kraland notifications", e);
        }
    }
}
