package arn.roub.krabot.domain.port.out;

import arn.roub.krabot.domain.model.KramailId;
import arn.roub.krabot.domain.model.NotificationState;
import arn.roub.krabot.domain.model.ReleaseVersion;

import java.util.Set;

/**
 * Port secondaire pour la persistance de l'état.
 */
public interface StateRepositoryPort {

    /**
     * Récupère l'état actuel.
     *
     * @return l'état actuel
     */
    NotificationState getState();

    /**
     * Met à jour le nombre de kramails.
     *
     * @param count le nombre de kramails
     */
    void updateKramailCount(int count);

    /**
     * Met à jour le flag de notification.
     *
     * @param hasNotification true si notification présente
     */
    void updateNotificationFlag(boolean hasNotification);

    /**
     * Met à jour la dernière version connue.
     *
     * @param version la dernière version
     */
    void updateLatestVersion(ReleaseVersion version);

    /**
     * Vérifie si un kramail a déjà été notifié.
     *
     * @param kramailId l'id du kramail
     * @return true si déjà notifié
     */
    boolean isKramailAlreadyNotified(KramailId kramailId);

    /**
     * Marque un kramail comme notifié.
     *
     * @param kramailId l'id du kramail
     */
    void markKramailAsNotified(KramailId kramailId);

    /**
     * Nettoie les kramails qui ne sont plus présents.
     *
     * @param currentKramailIds les ids des kramails actuels
     */
    void cleanupOldKramails(Set<KramailId> currentKramailIds);

    /**
     * Réinitialise le flag de notification générale.
     */
    void resetGeneralNotificationFlag();

    /**
     * Vérifie si la notification générale a déjà été envoyée.
     *
     * @return true si déjà envoyée
     */
    boolean isGeneralNotificationAlreadySent();

    /**
     * Marque la notification générale comme envoyée.
     */
    void markGeneralNotificationAsSent();
}
