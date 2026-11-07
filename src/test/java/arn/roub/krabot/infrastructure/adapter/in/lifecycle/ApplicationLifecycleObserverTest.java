package arn.roub.krabot.infrastructure.adapter.in.lifecycle;

import arn.roub.krabot.domain.port.in.ApplicationLifecycleUseCase;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ApplicationLifecycleObserverTest {

    @Test
    void relaysStartupAndShutdown() {
        ApplicationLifecycleUseCase useCase = mock(ApplicationLifecycleUseCase.class);
        ApplicationLifecycleObserver observer = new ApplicationLifecycleObserver(useCase);

        observer.onStart(null);
        verify(useCase).initialize();
        verify(useCase, never()).shutdown();

        observer.onStop(null);
        verify(useCase).shutdown();
    }
}
