package arn.roub.krabot.domain.model;

/**
 * Entité représentant un message Kraland (Kramail).
 */
public record Kramail(
        KramailId id,
        String title,
        String originator,
        String recipient,
        String section
) {

    public Kramail {
        if (id == null) {
            throw new IllegalArgumentException("Kramail id cannot be null");
        }
    }
}
