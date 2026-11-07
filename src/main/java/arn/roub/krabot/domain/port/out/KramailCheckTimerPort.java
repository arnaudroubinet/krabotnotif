package arn.roub.krabot.domain.port.out;

import java.time.Instant;

/**
 * Port secondaire pilotant le minuteur de vérification des kramails.
 */
public interface KramailCheckTimerPort {

    /**
     * Décale la prochaine vérification des kramails si elle est imminente.
     *
     * @return l'heure de la prochaine exécution
     */
    Instant postponeIfImminent();
}
