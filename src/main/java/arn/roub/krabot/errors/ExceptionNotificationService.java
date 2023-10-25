package arn.roub.krabot.errors;

import arn.roub.krabot.utils.DiscordWebhook;
import arn.roub.krabot.utils.PostponedNotificationException;
import arn.roub.krabot.utils.elements.EmbedObject;
import arn.roub.krabot.utils.elements.Field;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;


import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;

@ApplicationScoped
public class ExceptionNotificationService {

    private final String hookUrl;
    private final String avatar;
    private final String username;

    public ExceptionNotificationService(
            @ConfigProperty(name = "discord.hook.url") String hookUrl,
            @ConfigProperty(name = "discord.hook.avatar.url") String avatar,
            @ConfigProperty(name = "discord.hook.username") String username) {
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
            discordWebhook.addEmbed(
                    EmbedObject.builder()
                            .title("Exception occur !!")
                            .color(Color.RED)
                            .description(ex.getMessage())
                            .field(Field.builder()
                                    .name("Stacktrace")
                                    .value(sw.toString())
                                    .inline(false).build()
                            ).build());

            discordWebhook.setTts(false);
            discordWebhook.execute();
        } catch (PostponedNotificationException pnex) {
            //Do nothing
        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

}
