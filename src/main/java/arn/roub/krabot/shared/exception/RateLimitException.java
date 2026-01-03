package arn.roub.krabot.shared.exception;

/**
 * Exception levée lors d'un rate limit.
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
