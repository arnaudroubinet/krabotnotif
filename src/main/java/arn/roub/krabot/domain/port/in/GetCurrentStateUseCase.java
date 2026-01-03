package arn.roub.krabot.domain.port.in;

import arn.roub.krabot.domain.model.NotificationState;

/**
 * Port primaire pour récupérer l'état actuel des notifications.
 */
public interface GetCurrentStateUseCase {

    /**
     * Retourne l'état actuel du système de notifications.
     *
     * @return l'état actuel
     */
    NotificationState execute();
}
