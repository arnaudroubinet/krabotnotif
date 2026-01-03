package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.model.ReleaseVersion;
import arn.roub.krabot.domain.port.in.CheckReleaseUseCase;
import arn.roub.krabot.domain.port.out.GithubReleasePort;
import arn.roub.krabot.domain.port.out.NotificationPort;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implémentation du use case de vérification des releases.
 */
public class CheckReleaseUseCaseImpl implements CheckReleaseUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckReleaseUseCaseImpl.class);
    private static final int MAX_RETRIES = 3;

    private final GithubReleasePort githubReleasePort;
    private final NotificationPort notificationPort;
    private final StateRepositoryPort stateRepositoryPort;

    public CheckReleaseUseCaseImpl(
            GithubReleasePort githubReleasePort,
            NotificationPort notificationPort,
            StateRepositoryPort stateRepositoryPort
    ) {
        this.githubReleasePort = githubReleasePort;
        this.notificationPort = notificationPort;
        this.stateRepositoryPort = stateRepositoryPort;
    }

    @Override
    public void execute() {
        retryOnFailure(this::checkAndNotify);
    }

    private void checkAndNotify() {
        ReleaseVersion latestVersion = githubReleasePort.getLatestRelease();
        ReleaseVersion currentLatest = stateRepositoryPort.getState().latestVersion();

        if (latestVersion.isNewerThan(currentLatest)) {
            stateRepositoryPort.updateLatestVersion(latestVersion);
            notificationPort.sendReleaseNotification(latestVersion);
        }
    }

    private void retryOnFailure(Runnable operation) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                operation.run();
                return;
            } catch (RuntimeException ex) {
                LOGGER.warn("Attempt {} failed: {}", attempt + 1, ex.getMessage());
                if (attempt == MAX_RETRIES - 1) {
                    throw ex;
                }
            }
        }
    }
}
