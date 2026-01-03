package arn.roub.krabot.shared.exception;

/**
 * Exception levée lors d'erreurs d'envoi de notification.
 */
public class NotificationException extends RuntimeException {

    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
