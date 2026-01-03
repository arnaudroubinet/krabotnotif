package arn.roub.krabot.domain.port.in;

/**
 * Port primaire pour vérifier les kramails et envoyer des notifications.
 */
public interface CheckKramailsUseCase {

    /**
     * Vérifie les nouveaux kramails sur Kraland et envoie des notifications si nécessaire.
     */
    void execute();
}
