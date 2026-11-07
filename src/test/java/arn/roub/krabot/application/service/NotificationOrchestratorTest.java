package arn.roub.krabot.application.service;

import arn.roub.krabot.domain.model.ReleaseVersion;
import arn.roub.krabot.domain.port.out.GithubReleasePort;
import arn.roub.krabot.domain.port.out.NotificationPort;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;
import arn.roub.krabot.shared.exception.GithubApiException;
import arn.roub.krabot.shared.exception.NotificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationOrchestratorTest {

    @Mock
    private NotificationPort notificationPort;
    @Mock
    private GithubReleasePort githubReleasePort;
    @Mock
    private StateRepositoryPort stateRepositoryPort;
    @InjectMocks
    private NotificationOrchestrator orchestrator;

    @Test
    void initialize_storesLatestReleaseAndSendsStartupNotification() {
        ReleaseVersion latest = ReleaseVersion.of("v3.0.0");
        when(githubReleasePort.getLatestRelease()).thenReturn(latest);

        orchestrator.initialize();

        verify(stateRepositoryPort).updateLatestVersion(latest);
        verify(notificationPort).sendStartupNotification();
    }

    @Test
    void initialize_isNotFatalWhenGithubFails() {
        when(githubReleasePort.getLatestRelease()).thenThrow(new GithubApiException("down"));

        assertDoesNotThrow(orchestrator::initialize);

        verify(stateRepositoryPort, never()).updateLatestVersion(any());
    }

    @Test
    void shutdown_sendsShutdownNotification() {
        orchestrator.shutdown();

        verify(notificationPort).sendShutdownNotification();
    }

    @Test
    void shutdown_swallowsNotificationFailure() {
        doThrow(new NotificationException("ko")).when(notificationPort).sendShutdownNotification();

        assertDoesNotThrow(orchestrator::shutdown);
    }

    @Test
    void notifyError_usesExceptionMessage() {
        orchestrator.notifyError(new IllegalStateException("kaboom"));

        verify(notificationPort).sendErrorNotification("kaboom");
    }

    @Test
    void notifyError_fallsBackToExceptionTypeWhenNoMessage() {
        orchestrator.notifyError(new IllegalStateException());

        verify(notificationPort).sendErrorNotification("IllegalStateException");
    }

    @Test
    void notifyError_neverThrows() {
        doThrow(new NotificationException("ko")).when(notificationPort).sendErrorNotification(any());

        assertDoesNotThrow(() -> orchestrator.notifyError(new RuntimeException("x")));
    }
}
