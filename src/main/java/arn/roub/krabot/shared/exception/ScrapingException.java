package arn.roub.krabot.shared.exception;

/**
 * Exception levée lors d'erreurs de scraping.
 */
public class ScrapingException extends RuntimeException {

    public ScrapingException(String message) {
        super(message);
    }

    public ScrapingException(String message, Throwable cause) {
        super(message, cause);
    }
}
