package arn.roub.krabot.domain.port.in;

/**
 * Port primaire pour réinitialiser l'état de la notification générale.
 */
public interface ResetGeneralNotificationUseCase {

    /**
     * Réinitialise l'état de la notification générale.
     * Permet de recevoir une nouvelle notification générale lors du prochain scan.
     */
    void execute();
}

