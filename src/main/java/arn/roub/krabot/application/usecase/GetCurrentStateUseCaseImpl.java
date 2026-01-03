package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.model.NotificationState;
import arn.roub.krabot.domain.port.in.GetCurrentStateUseCase;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;

/**
 * Implémentation du use case de récupération de l'état courant.
 */
public class GetCurrentStateUseCaseImpl implements GetCurrentStateUseCase {

    private final StateRepositoryPort stateRepositoryPort;

    public GetCurrentStateUseCaseImpl(StateRepositoryPort stateRepositoryPort) {
        this.stateRepositoryPort = stateRepositoryPort;
    }

    @Override
    public NotificationState execute() {
        return stateRepositoryPort.getState();
    }
}
