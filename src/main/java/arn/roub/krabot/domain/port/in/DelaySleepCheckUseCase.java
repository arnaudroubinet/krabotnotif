package arn.roub.krabot.domain.port.in;

import java.time.Instant;

/**
 * Port primaire pour décaler la prochaine vérification du rappel de sommeil.
 */
public interface DelaySleepCheckUseCase {

    /**
     * Décale la prochaine exécution de la vérification du rappel de sommeil.
     *
     * @return l'heure de la prochaine exécution
     */
    Instant delay();
}
