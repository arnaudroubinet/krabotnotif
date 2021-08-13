package arn.roub.hook.scrapper;

import org.springframework.stereotype.Service;

import java.net.CookieManager;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class ScrappingClient {

    private final HttpClient httpClient;
    private final HttpRequest loadKi;
    private final HttpRequest authKi;

    public ScrappingClient() {
        try {
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .cookieHandler(new CookieManager())
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .priority(1)
                    .proxy(ProxySelector.getDefault())
                    .version(HttpClient.Version.HTTP_2)
                    .build();


            loadKi = HttpRequest.newBuilder(new URI("http://www.kraland.org/main.php?p=1")).GET().build();
            authKi = HttpRequest.newBuilder(new URI("http://www.kraland.org/main.php?p=1&a=100"))
                    .POST(HttpRequest.BodyPublishers.ofString("p1=thesith&p2=Thesith!&Submit=Ok!"))
                    .headers("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0",
                            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                            "Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            "Accept-Encoding", "gzip, deflate",
                            "Content-Type", "application/x-www-form-urlencoded",
                            "Origin", "http://www.kraland.org",
                            "DNT", "1",
                            "Referer", "http://www.kraland.org/main.php",
                            "Upgrade-Insecure-Requests", "1").build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean hasNotification() {
        try {
            HttpResponse<String> response = httpClient.send(loadKi, HttpResponse.BodyHandlers.ofString());
            if (response.body().contains("IDENTIFIER")) {
                httpClient.send(authKi, HttpResponse.BodyHandlers.ofString());
                response = httpClient.send(loadKi, HttpResponse.BodyHandlers.ofString());
            }

            return (!response.body().contains("report.gif")) && response.body().contains("Rowanne");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
