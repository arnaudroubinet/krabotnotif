package arn.roub.hook.utils;

public class PostponedNotificationException extends RuntimeException {

    public PostponedNotificationException() {
    }

    public PostponedNotificationException(String message) {
        super(message);
    }

    public PostponedNotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PostponedNotificationException(Throwable cause) {
        super(cause);
    }

    public PostponedNotificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
