package arn.roub.krabot.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Discord webhook configuration properties.
 * Centralized configuration for all Discord-related settings.
 */
@ConfigMapping(prefix = "discord.hook")
public interface DiscordConfig {
    
    /**
     * Discord webhook URL (required)
     */
    String url();
    
    /**
     * Avatar URL for the webhook bot
     */
    @WithDefault("http://img.kraland.org/a/krabot.jpg")
    String avatarUrl();
    
    /**
     * Username displayed in Discord
     */
    @WithDefault("Krabot")
    String username();
    
    /**
     * Message template for kramails.
     * Supports placeholders: *originator*, *title*
     */
    @WithDefault("Hey, tu as un kramail de '*originator*' ayant pour sujet '*title*' !!")
    String messageKramail();
    
    /**
     * Message for general notifications
     */
    @WithDefault("Hey, tu as une notification !!")
    String messageNotification();
    
    /**
     * First message sent when bot starts
     */
    @WithDefault("Krabot est de retour... pour vous jouer un mauvais tour !")
    String firstMessage();
    
    /**
     * Last message sent when bot shuts down
     */
    @WithDefault("Je m'en vais, au revoir !")
    String lastMessage();
    
    /**
     * Message for new releases
     */
    @WithDefault("Une nouvelle release de KrabotNotif est disponible")
    String release();
    
    /**
     * Error message prefix
     */
    @WithDefault("Oh no !")
    String errorPrefixMessage();
}
