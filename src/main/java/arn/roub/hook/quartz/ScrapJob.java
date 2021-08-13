package arn.roub.hook.quartz;

import arn.roub.hook.DiscordWebhook;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ScrapJob implements Job {

    @Value("${discord.hook.url}")
    private String hookUrl;

    @Value("${discord.hook.avatar.url}")
    private String avatar;

    @Value("${discord.hook.username}")
    private String username;

    private static final HttpClient HTTP_CLIENT;

    private static final HttpRequest.Builder LOAD_KI;
    private static final HttpRequest.Builder AUTH_KI;

    static {
        try {
            CookieHandler.setDefault(new CookieManager());
            HTTP_CLIENT = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .cookieHandler(CookieHandler.getDefault())
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .priority(1)
                    .proxy(ProxySelector.getDefault())
                    .version(HttpClient.Version.HTTP_2)
                    .build();


            LOAD_KI = HttpRequest.newBuilder(new URI("http://www.kraland.org/main.php?p=1")).GET();
            AUTH_KI = HttpRequest.newBuilder(new URI("http://www.kraland.org/main.php?p=1&a=100"))
                    .POST( HttpRequest.BodyPublishers.ofString("p1=thesith&p2=Thesith!&Submit=Ok!"))
                    .headers("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0",
                            "Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                            "Accept-Language","fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            "Accept-Encoding","gzip, deflate",
                            "Content-Type","application/x-www-form-urlencoded",
                            "Origin","http://www.kraland.org",
                            "DNT","1",
                            "Referer","http://www.kraland.org/main.php",
                            //"Cookie","PHPSESSID=c8570c5a5bb090f8ba8e1eeb7c979ffe; cookieconsent_dismissed=yes; pc_id=8786812678501",
                            "Upgrade-Insecure-Requests","1");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static final AtomicBoolean init = new AtomicBoolean(false);
    private static final AtomicBoolean messageWaiting = new AtomicBoolean(false);


    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            if (!init.get()) {
                DiscordWebhook discordWebhook = new DiscordWebhook(hookUrl);
                discordWebhook.setAvatarUrl(avatar);
                discordWebhook.setUsername(username);
                discordWebhook.setContent("Hello you");
                discordWebhook.setTts(false);

                //https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb
                discordWebhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle("Krabot report hook is alive !!")
                        .setDescription("The kraland report scrapper is live.")
                        .setColor(Color.RED)
                        .setImage("http://img.kraland.org/2/cyb/flag1.gif")
                        .setUrl("http://www.kraland.org/report.php"));

                try {
                    discordWebhook.execute();
                    init.set(true);
                } catch (IOException e) {
                    throw new JobExecutionException(e);
                }
            }

            //Scrape

            if (!hasMessage()) {
                messageWaiting.set(false);
            } else if (!messageWaiting.get()) {
                //report not empty and message was not already trigger
                messageWaiting.set(true);
                String content = "Hey you have message !!";

                DiscordWebhook discordWebhook = new DiscordWebhook(hookUrl);
                discordWebhook.setAvatarUrl(avatar);
                discordWebhook.setUsername(username);
                discordWebhook.setContent(content);
                discordWebhook.setTts(false);
                discordWebhook.execute();

            }

        } catch (Exception e) {
            throw new JobExecutionException(e);

        }
    }

    private boolean hasMessage() throws IOException, InterruptedException {
        HttpResponse<String> response = HTTP_CLIENT.send(LOAD_KI.build(), HttpResponse.BodyHandlers.ofString());

        if (response.body().contains("IDENTIFIER")) {
            HTTP_CLIENT.send(AUTH_KI.build(), HttpResponse.BodyHandlers.ofString());
            response = HTTP_CLIENT.send(LOAD_KI.build(), HttpResponse.BodyHandlers.ofString());
        }

        return (!response.body().contains("report.gif")) && response.body().contains("Rowanne");

    }
}
