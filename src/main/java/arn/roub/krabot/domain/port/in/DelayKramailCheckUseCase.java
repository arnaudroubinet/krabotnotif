package arn.roub.krabot.domain.port.in;

import java.time.Instant;

/**
 * Port primaire appelé lorsque le joueur est actif sur Kraland (heartbeat).
 */
public interface DelayKramailCheckUseCase {

    /**
     * Réinitialise l'état des notifications (le joueur voit déjà ses kramails/notifications)
     * et décale la prochaine vérification des kramails si elle est imminente.
     *
     * @return l'heure de la prochaine exécution
     */
    Instant delay();
}
