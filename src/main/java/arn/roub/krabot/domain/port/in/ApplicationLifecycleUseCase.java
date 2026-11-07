package arn.roub.krabot.domain.port.in;

/**
 * Port primaire pour le cycle de vie de l'application (démarrage / arrêt).
 */
public interface ApplicationLifecycleUseCase {

    /**
     * Initialise l'application : récupère la dernière release et notifie le démarrage.
     */
    void initialize();

    /**
     * Notifie l'arrêt de l'application.
     */
    void shutdown();
}
