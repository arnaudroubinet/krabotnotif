package arn.roub.krabot.domain.port.in;

import java.time.Instant;

/**
 * Port primaire pour décaler la prochaine vérification des kramails.
 */
public interface DelayKramailCheckUseCase {

    /**
     * Décale la prochaine exécution de la vérification des kramails.
     *
     * @return l'heure de la prochaine exécution
     */
    Instant delay();
}
