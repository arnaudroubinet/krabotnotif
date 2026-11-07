package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.port.out.KramailCheckTimerPort;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DelayKramailCheckUseCaseImplTest {

    @Mock
    private StateRepositoryPort stateRepositoryPort;
    @Mock
    private KramailCheckTimerPort timerPort;
    @InjectMocks
    private DelayKramailCheckUseCaseImpl useCase;

    @Test
    void resetsNotificationStatesThenPostponesTimer() {
        Instant next = Instant.parse("2026-01-01T10:00:00Z");
        when(timerPort.postponeIfImminent()).thenReturn(next);

        Instant result = useCase.delay();

        assertEquals(next, result);
        InOrder inOrder = inOrder(stateRepositoryPort, timerPort);
        inOrder.verify(stateRepositoryPort).resetGeneralNotificationState();
        inOrder.verify(stateRepositoryPort).resetKramailsNotificationState();
        inOrder.verify(timerPort).postponeIfImminent();
    }
}
