package arn.roub.krabot.domain.model;

import java.time.Instant;

/**
 * Résumé d'un utilisateur pour liste.
 */
public record UserSummary(String playerId, String name, int pp, Instant updatedAt) {}
