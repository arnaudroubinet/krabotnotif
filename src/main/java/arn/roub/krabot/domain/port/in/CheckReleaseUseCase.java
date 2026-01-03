package arn.roub.krabot.domain.port.in;

/**
 * Port primaire pour vérifier les nouvelles releases GitHub.
 */
public interface CheckReleaseUseCase {

    /**
     * Vérifie s'il y a une nouvelle release sur GitHub et envoie une notification si nécessaire.
     */
    void execute();
}
