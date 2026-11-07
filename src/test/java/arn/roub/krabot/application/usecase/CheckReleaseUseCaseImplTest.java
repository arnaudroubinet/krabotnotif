package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.model.NotificationState;
import arn.roub.krabot.domain.model.ReleaseVersion;
import arn.roub.krabot.domain.port.out.GithubReleasePort;
import arn.roub.krabot.domain.port.out.NotificationPort;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;
import arn.roub.krabot.shared.exception.GithubApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckReleaseUseCaseImplTest {

    private static final ReleaseVersion CURRENT = ReleaseVersion.of("v1.0.0");

    @Mock
    private GithubReleasePort githubReleasePort;
    @Mock
    private NotificationPort notificationPort;
    @Mock
    private StateRepositoryPort stateRepositoryPort;

    private CheckReleaseUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CheckReleaseUseCaseImpl(githubReleasePort, notificationPort, stateRepositoryPort);
    }

    private void knownLatest(ReleaseVersion latest) {
        when(stateRepositoryPort.getState())
                .thenReturn(NotificationState.initial(CURRENT).withLatestVersion(latest));
    }

    @Test
    void newRelease_updatesStateAndNotifies() {
        knownLatest(ReleaseVersion.of("v1.0.0"));
        ReleaseVersion v2 = ReleaseVersion.of("v2.0.0");
        when(githubReleasePort.getLatestRelease()).thenReturn(v2);

        useCase.execute();

        verify(stateRepositoryPort).updateLatestVersion(v2);
        verify(notificationPort).sendReleaseNotification(v2);
    }

    @Test
    void sameRelease_doesNothing() {
        knownLatest(ReleaseVersion.of("v1.0.0"));
        when(githubReleasePort.getLatestRelease()).thenReturn(ReleaseVersion.of("v1.0.0"));

        useCase.execute();

        verify(stateRepositoryPort, never()).updateLatestVersion(any());
        verifyNoInteractions(notificationPort);
    }

    @Test
    void retriesOnTransientFailure() {
        knownLatest(ReleaseVersion.of("v1.0.0"));
        when(githubReleasePort.getLatestRelease())
                .thenThrow(new GithubApiException("rate limit"))
                .thenReturn(ReleaseVersion.of("v1.0.0"));

        useCase.execute();

        verify(githubReleasePort, times(2)).getLatestRelease();
    }

    @Test
    void throwsAfterMaxRetries() {
        when(githubReleasePort.getLatestRelease()).thenThrow(new GithubApiException("down"));

        assertThrows(GithubApiException.class, useCase::execute);

        verify(githubReleasePort, times(3)).getLatestRelease();
        verifyNoInteractions(notificationPort);
    }
}
