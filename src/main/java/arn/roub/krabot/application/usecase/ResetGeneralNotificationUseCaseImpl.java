package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.port.in.ResetGeneralNotificationUseCase;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;

/**
 * Implémentation du use case de réinitialisation de l'état de la notification générale.
 */
public class ResetGeneralNotificationUseCaseImpl implements ResetGeneralNotificationUseCase {

    private final StateRepositoryPort stateRepositoryPort;

    public ResetGeneralNotificationUseCaseImpl(StateRepositoryPort stateRepositoryPort) {
        this.stateRepositoryPort = stateRepositoryPort;
    }

    @Override
    public void execute() {
        stateRepositoryPort.resetGeneralNotificationState();
    }
}

