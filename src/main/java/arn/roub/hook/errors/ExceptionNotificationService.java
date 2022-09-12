package arn.roub.hook.errors;

import arn.roub.hook.utils.DiscordWebhook;
import arn.roub.hook.utils.PostponedNotificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class ExceptionNotificationService {

    private final String hookUrl;
    private final String avatar;
    private final String username;

    public ExceptionNotificationService(
            @Value("${discord.hook.url}") String hookUrl,
            @Value("${discord.hook.avatar.url}") String avatar,
            @Value("${discord.hook.username}") String username) {
        this.hookUrl = hookUrl;
        this.avatar = avatar;
        this.username = username;
    }

    public void exceptionManagement(Throwable ex) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            DiscordWebhook discordWebhook = new DiscordWebhook(hookUrl);
            discordWebhook.setAvatarUrl(avatar);
            discordWebhook.setUsername(username);
            discordWebhook.setContent("Help me !!");
            discordWebhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("Exception occur !!")
                    .setColor(Color.RED)
                    .setDescription(ex.getMessage())
                    .addField("Stacktrace", sw.toString(),false));

            discordWebhook.setTts(false);
            discordWebhook.execute();
        } catch (PostponedNotificationException pnex) {
            //Do nothing
        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

}
