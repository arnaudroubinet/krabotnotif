package arn.roub.krabot.domain.model;

import java.time.Instant;

/**
 * Immutable domain record for character characteristics.
 */
public record Characteristic(String playerId, String name, int pp, Instant updatedAt) {
    public Characteristic(String playerId, String name, int pp) {
        this(playerId, name, pp, Instant.now());
    }
}