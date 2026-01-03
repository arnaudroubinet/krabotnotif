package arn.roub.krabot.domain.model;

/**
 * Value Object représentant les credentials d'un compte Kraland.
 */
public record Account(String username, String password) {

    public Account {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
    }
}
