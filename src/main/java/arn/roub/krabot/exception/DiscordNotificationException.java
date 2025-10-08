package arn.roub.krabot.exception;

/**
 * Exception thrown when Discord webhook notification fails.
 */
public class DiscordNotificationException extends RuntimeException {
    
    public DiscordNotificationException(String message) {
        super(message);
    }
    
    public DiscordNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
