package arn.roub.krabot.domain.port.in;

/**
 * Port primaire pour réinitialiser l'état des notifications de kramails.
 */
public interface ResetKramailsNotificationUseCase {

    /**
     * Réinitialise tous les états des kramails notifiés.
     * Permet de recevoir de nouvelles notifications de kramails lors du prochain scan.
     */
    void execute();
}

