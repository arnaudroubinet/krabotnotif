package arn.roub.krabot.infrastructure.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration du webhook Discord.
 */
@ConfigMapping(prefix = "discord.hook")
public interface DiscordConfig {

    /**
     * URL du webhook Discord (requis)
     */
    String url();

    /**
     * URL de l'avatar du bot
     */
    @WithDefault("http://img.kraland.org/a/krabot.jpg")
    String avatarUrl();

    /**
     * Nom d'utilisateur affiché dans Discord
     */
    @WithDefault("Krabot")
    String username();

    /**
     * Template de message pour les kramails.
     * Placeholders: *originator*, *title*, *recipient*
     */
    @WithDefault("Hey, tu as un kramail de '*originator*' ayant pour sujet '*title*' !!")
    String messageKramail();

    /**
     * Message pour les notifications générales
     */
    @WithDefault("Hey, tu as une notification !!")
    String messageNotification();

    /**
     * Message de démarrage du bot
     */
    @WithDefault("Krabot est de retour... pour vous jouer un mauvais tour !")
    String firstMessage();

    /**
     * Message d'arrêt du bot
     */
    @WithDefault("Je m'en vais, au revoir !")
    String lastMessage();

    /**
     * Message pour les nouvelles releases
     */
    @WithDefault("Une nouvelle release de KrabotNotif est disponible")
    String release();

    /**
     * Préfixe pour les messages d'erreur
     */
    @WithDefault("Oh no !")
    String errorPrefixMessage();

    /**
     * Message de rappel de sommeil
     */
    @WithDefault("N'oublie pas de dormir")
    String messageSleep();
}
