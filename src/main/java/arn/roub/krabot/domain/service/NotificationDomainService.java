package arn.roub.krabot.domain.service;

import arn.roub.krabot.domain.model.Kramail;
import arn.roub.krabot.domain.model.KramailId;
import arn.roub.krabot.domain.model.ScrapingResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service de domaine contenant la logique métier de notification.
 */
public final class NotificationDomainService {

    /**
     * Détermine quels kramails nécessitent une notification.
     *
     * @param scrapingResult le résultat du scraping
     * @param alreadyNotifiedChecker fonction pour vérifier si un kramail a déjà été notifié
     * @return la liste des kramails à notifier
     */
    public List<Kramail> findKramailsToNotify(
            ScrapingResult scrapingResult,
            KramailNotifiedChecker alreadyNotifiedChecker
    ) {
        return scrapingResult.kramails().stream()
                .filter(kramail -> !alreadyNotifiedChecker.isNotified(kramail.id()))
                .toList();
    }

    /**
     * Extrait les IDs des kramails d'un résultat de scraping.
     *
     * @param scrapingResult le résultat du scraping
     * @return l'ensemble des IDs
     */
    public Set<KramailId> extractKramailIds(ScrapingResult scrapingResult) {
        return scrapingResult.kramails().stream()
                .map(Kramail::id)
                .collect(Collectors.toSet());
    }

    /**
     * Détermine si une notification générale doit être envoyée.
     *
     * @param scrapingResult le résultat du scraping
     * @param alreadySent true si la notification a déjà été envoyée
     * @return true si la notification doit être envoyée
     */
    public boolean shouldSendGeneralNotification(ScrapingResult scrapingResult, boolean alreadySent) {
        return scrapingResult.hasNotification() && !alreadySent;
    }

    /**
     * Interface fonctionnelle pour vérifier si un kramail a déjà été notifié.
     */
    @FunctionalInterface
    public interface KramailNotifiedChecker {
        boolean isNotified(KramailId kramailId);
    }
}
