package arn.roub.krabot.exception;

/**
 * Exception thrown when scraping Kraland website fails.
 */
public class KralandScrapingException extends RuntimeException {
    
    public KralandScrapingException(String message) {
        super(message);
    }
    
    public KralandScrapingException(String message, Throwable cause) {
        super(message, cause);
    }
}
