package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.port.in.ResetKramailsNotificationUseCase;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;

/**
 * Implémentation du use case de réinitialisation de l'état des notifications de kramails.
 */
public class ResetKramailsNotificationUseCaseImpl implements ResetKramailsNotificationUseCase {

    private final StateRepositoryPort stateRepositoryPort;

    public ResetKramailsNotificationUseCaseImpl(StateRepositoryPort stateRepositoryPort) {
        this.stateRepositoryPort = stateRepositoryPort;
    }

    @Override
    public void execute() {
        stateRepositoryPort.resetKramailsNotificationState();
    }
}

