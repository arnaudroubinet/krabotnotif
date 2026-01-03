package arn.roub.krabot.shared.exception;

/**
 * Exception levée lors d'erreurs avec l'API GitHub.
 */
public class GithubApiException extends RuntimeException {

    public GithubApiException(String message) {
        super(message);
    }

    public GithubApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
