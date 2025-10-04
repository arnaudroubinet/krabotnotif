package arn.roub.krabot.errors;

import arn.roub.krabot.utils.DiscordWebhook;
import arn.roub.krabot.utils.PostponedNotificationException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ExceptionNotificationService {

    private final String hookUrl;
    private final String avatar;
    private final String username;
    private final String prefixMessage;

    public ExceptionNotificationService(
            @ConfigProperty(name = "discord.hook.url") String hookUrl,
            @ConfigProperty(name = "discord.hook.avatar.url") String avatar,
            @ConfigProperty(name = "discord.hook.username") String username,
            @ConfigProperty(name = "discord.hook.error.prefix-message") String prefixMessage) {
        this.hookUrl = hookUrl;
        this.avatar = avatar;
        this.username = username;
        this.prefixMessage = prefixMessage;
    }

    public void exceptionManagement(Throwable ex) {
        try {
            DiscordWebhook discordWebhook = new DiscordWebhook(hookUrl);
            discordWebhook.setAvatarUrl(avatar);
            discordWebhook.setUsername(username);
            discordWebhook.setContent(prefixMessage +" "+ ex.getMessage());
            discordWebhook.execute();
        } catch (PostponedNotificationException pnex) {
            //Do nothing
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
