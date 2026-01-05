package arn.roub.krabot.domain.port.out;

import arn.roub.krabot.domain.model.Account;
import arn.roub.krabot.domain.model.ScrapingResult;

/**
 * Port secondaire pour le scraping de Kraland.
 */
public interface KralandScrapingPort {

    /**
     * Scrape le site Kraland pour récupérer les kramails et notifications.
     *
     * @param account les credentials du compte Kraland
     * @return le résultat du scraping
     */
    ScrapingResult scrape(Account account);

    /**
     * Vérifie si l'action "Dormir" est disponible sur la page plateau.
     *
     * @param account les credentials du compte Kraland
     * @return true si le bouton Dormir est disponible (btn-primary)
     */
    boolean isSleepAvailable(Account account);
}
