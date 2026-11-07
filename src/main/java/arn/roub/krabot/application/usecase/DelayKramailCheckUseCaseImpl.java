package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.port.in.DelayKramailCheckUseCase;
import arn.roub.krabot.domain.port.out.KramailCheckTimerPort;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;

import java.time.Instant;

/**
 * Implémentation du use case de décalage de la vérification des kramails.
 */
public class DelayKramailCheckUseCaseImpl implements DelayKramailCheckUseCase {

    private final StateRepositoryPort stateRepositoryPort;
    private final KramailCheckTimerPort kramailCheckTimerPort;

    public DelayKramailCheckUseCaseImpl(
            StateRepositoryPort stateRepositoryPort,
            KramailCheckTimerPort kramailCheckTimerPort
    ) {
        this.stateRepositoryPort = stateRepositoryPort;
        this.kramailCheckTimerPort = kramailCheckTimerPort;
    }

    @Override
    public Instant delay() {
        stateRepositoryPort.resetGeneralNotificationState();
        stateRepositoryPort.resetKramailsNotificationState();
        return kramailCheckTimerPort.postponeIfImminent();
    }
}
