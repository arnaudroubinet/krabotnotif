package arn.roub.krabot.domain.port.out;

import arn.roub.krabot.domain.model.Kramail;
import arn.roub.krabot.domain.model.ReleaseVersion;

/**
 * Port secondaire pour l'envoi de notifications.
 */
public interface NotificationPort {

    /**
     * Envoie une notification de démarrage.
     */
    void sendStartupNotification();

    /**
     * Envoie une notification d'arrêt.
     */
    void sendShutdownNotification();

    /**
     * Envoie une notification pour un nouveau kramail.
     *
     * @param kramail le kramail à notifier
     */
    void sendKramailNotification(Kramail kramail);

    /**
     * Envoie une notification générale (report).
     */
    void sendGeneralNotification();

    /**
     * Envoie une notification de nouvelle release.
     *
     * @param version la nouvelle version
     */
    void sendReleaseNotification(ReleaseVersion version);

    /**
     * Envoie une notification d'erreur.
     *
     * @param message le message d'erreur
     */
    void sendErrorNotification(String message);

    /**
     * Envoie un rappel de sommeil.
     */
    void sendSleepReminderNotification();
}
