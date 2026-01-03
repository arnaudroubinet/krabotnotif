package arn.roub.krabot.infrastructure.config;

import io.smallrye.config.ConfigMapping;

/**
 * Configuration des credentials Kraland.
 */
@ConfigMapping(prefix = "kraland")
public interface KralandConfig {

    /**
     * Nom d'utilisateur Kraland
     */
    String user();

    /**
     * Mot de passe Kraland
     */
    String password();
}
