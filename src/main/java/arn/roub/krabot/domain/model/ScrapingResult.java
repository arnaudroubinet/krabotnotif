package arn.roub.krabot.domain.model;

import java.util.List;

/**
 * Value Object représentant le résultat d'un scraping Kraland.
 */
public record ScrapingResult(
        List<Kramail> kramails,
        boolean hasNotification
) {

    public ScrapingResult {
        kramails = kramails != null ? List.copyOf(kramails) : List.of();
    }

    public static ScrapingResult empty() {
        return new ScrapingResult(List.of(), false);
    }

    public boolean hasKramails() {
        return !kramails.isEmpty();
    }
}
