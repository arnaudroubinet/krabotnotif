package arn.roub.krabot.application.service;

import arn.roub.krabot.domain.model.ReleaseVersion;
import arn.roub.krabot.domain.port.out.GithubReleasePort;
import arn.roub.krabot.domain.port.out.NotificationPort;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrateur pour les opérations de notification lifecycle (startup/shutdown).
 */
public class NotificationOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationOrchestrator.class);

    private final NotificationPort notificationPort;
    private final GithubReleasePort githubReleasePort;
    private final StateRepositoryPort stateRepositoryPort;

    public NotificationOrchestrator(
            NotificationPort notificationPort,
            GithubReleasePort githubReleasePort,
            StateRepositoryPort stateRepositoryPort
    ) {
        this.notificationPort = notificationPort;
        this.githubReleasePort = githubReleasePort;
        this.stateRepositoryPort = stateRepositoryPort;
    }

    /**
     * Initialise l'application : récupère la dernière version et envoie la notification de démarrage.
     */
    public void initialize() {
        try {
            ReleaseVersion latestVersion = githubReleasePort.getLatestRelease();
            stateRepositoryPort.updateLatestVersion(latestVersion);
            notificationPort.sendStartupNotification();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize: {}", e.getMessage(), e);
            // L'initialisation n'est pas fatale
        }
    }

    /**
     * Termine l'application : envoie la notification d'arrêt.
     */
    public void shutdown() {
        try {
            notificationPort.sendShutdownNotification();
        } catch (Exception e) {
            LOGGER.error("Failed to send shutdown notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifie une erreur.
     *
     * @param error l'exception à notifier
     */
    public void notifyError(Exception error) {
        try {
            String message = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
            notificationPort.sendErrorNotification(message);
        } catch (Exception e) {
            LOGGER.error("Failed to send error notification: {}", e.getMessage(), e);
        }
    }
}
