package arn.roub.krabot.domain.port.in;

/**
 * Port primaire pour signaler une erreur survenue lors d'un traitement.
 */
public interface ReportErrorUseCase {

    /**
     * Notifie une erreur. Ne lève jamais d'exception.
     *
     * @param error l'erreur à notifier
     */
    void notifyError(Exception error);
}
