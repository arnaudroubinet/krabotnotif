package arn.roub.krabot.domain.model;

/**
 * Value Object représentant l'identifiant unique d'un Kramail.
 */
public record KramailId(String value) {

    public KramailId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("KramailId cannot be null or blank");
        }
    }
}
