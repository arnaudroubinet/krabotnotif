package arn.roub.krabot.infrastructure.adapter.in.scheduler;

import arn.roub.krabot.domain.port.in.CheckKramailsUseCase;
import arn.roub.krabot.domain.port.in.ReportErrorUseCase;
import arn.roub.krabot.shared.exception.ScrapingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KramailCheckSchedulerTest {

    private final CheckKramailsUseCase useCase = mock(CheckKramailsUseCase.class);
    private final ReportErrorUseCase reportError = mock(ReportErrorUseCase.class);
    private KramailCheckScheduler scheduler;

    @AfterEach
    void tearDown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Test
    void runsUseCasePeriodically() {
        scheduler = new KramailCheckScheduler(useCase, reportError, Duration.ofMillis(20), Duration.ofMinutes(1));
        scheduler.start();

        verify(useCase, timeout(2000).atLeast(2)).execute();
        verifyNoInteractions(reportError);
    }

    @Test
    void reportsErrorsAndKeepsRunning() {
        doThrow(new ScrapingException("boom")).when(useCase).execute();
        scheduler = new KramailCheckScheduler(useCase, reportError, Duration.ofMillis(20), Duration.ofMinutes(1));
        scheduler.start();

        verify(reportError, timeout(2000).atLeast(2)).notifyError(any(ScrapingException.class));
    }

    @Test
    void postpone_doesNothingWhenNextRunIsNotImminent() {
        scheduler = new KramailCheckScheduler(useCase, reportError, Duration.ofMinutes(10), Duration.ofMinutes(1));
        scheduler.start();

        Instant first = scheduler.postponeIfImminent();
        Instant second = scheduler.postponeIfImminent();

        assertEquals(first, second);
        assertTrue(first.isAfter(Instant.now().plus(Duration.ofMinutes(9))));
    }

    @Test
    void postpone_shiftsImminentRunByDelayAmount() {
        scheduler = new KramailCheckScheduler(useCase, reportError, Duration.ofSeconds(30), Duration.ofMinutes(1));
        scheduler.start();

        Instant next = scheduler.postponeIfImminent();

        Duration untilNext = Duration.between(Instant.now(), next);
        assertTrue(untilNext.compareTo(Duration.ofSeconds(80)) > 0, "expected ~90s, got " + untilNext);
        assertTrue(untilNext.compareTo(Duration.ofSeconds(91)) <= 0, "expected ~90s, got " + untilNext);
        verifyNoInteractions(useCase);
    }

    @Test
    void postpone_withoutScheduledTaskReturnsNull() {
        scheduler = new KramailCheckScheduler(useCase, reportError, Duration.ofSeconds(30), Duration.ofMinutes(1));

        assertNull(scheduler.postponeIfImminent());
    }
}
