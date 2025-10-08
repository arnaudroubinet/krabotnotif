package arn.roub.krabot.exception;

/**
 * Exception thrown when GitHub API operations fail.
 */
public class GithubApiException extends RuntimeException {
    
    public GithubApiException(String message) {
        super(message);
    }
    
    public GithubApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
