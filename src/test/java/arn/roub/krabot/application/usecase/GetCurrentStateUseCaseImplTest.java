package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.model.NotificationState;
import arn.roub.krabot.domain.model.ReleaseVersion;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetCurrentStateUseCaseImplTest {

    @Test
    void returnsStateFromRepository() {
        StateRepositoryPort repository = mock(StateRepositoryPort.class);
        NotificationState state = NotificationState.initial(ReleaseVersion.of("v1.0.0")).withKramailCount(3);
        when(repository.getState()).thenReturn(state);

        assertSame(state, new GetCurrentStateUseCaseImpl(repository).execute());
    }
}
