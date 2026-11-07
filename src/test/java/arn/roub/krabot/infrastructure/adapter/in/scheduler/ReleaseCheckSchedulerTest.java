package arn.roub.krabot.infrastructure.adapter.in.scheduler;

import arn.roub.krabot.domain.port.in.CheckReleaseUseCase;
import arn.roub.krabot.domain.port.in.ReportErrorUseCase;
import arn.roub.krabot.shared.exception.GithubApiException;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReleaseCheckSchedulerTest {

    private final CheckReleaseUseCase useCase = mock(CheckReleaseUseCase.class);
    private final ReportErrorUseCase reportError = mock(ReportErrorUseCase.class);
    private final ReleaseCheckScheduler scheduler = new ReleaseCheckScheduler(useCase, reportError);

    @Test
    void executesUseCase() {
        scheduler.execute();

        verify(useCase).execute();
        verifyNoInteractions(reportError);
    }

    @Test
    void reportsErrors() {
        GithubApiException error = new GithubApiException("down");
        doThrow(error).when(useCase).execute();

        scheduler.execute();

        verify(reportError).notifyError(error);
    }

    @Test
    void doesNotReportWhenSuccessful() {
        scheduler.execute();

        verify(reportError, never()).notifyError(any());
    }
}
