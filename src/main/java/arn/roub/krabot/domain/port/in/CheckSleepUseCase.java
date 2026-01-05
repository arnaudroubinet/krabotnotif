package arn.roub.krabot.domain.port.in;

/**
 * Port primaire pour la vérification du rappel de sommeil.
 */
public interface CheckSleepUseCase {

    /**
     * Vérifie si l'action "Dormir" est disponible sur la page plateau
     * et envoie une notification si c'est le cas.
     */
    void execute();
}
