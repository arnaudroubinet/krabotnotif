package arn.roub.krabot.infrastructure.adapter.in.lifecycle;

import arn.roub.krabot.domain.port.in.ApplicationLifecycleUseCase;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * Adapter entrant : relaie les événements de cycle de vie Quarkus vers le domaine.
 */
@ApplicationScoped
public class ApplicationLifecycleObserver {

    private final ApplicationLifecycleUseCase applicationLifecycleUseCase;

    public ApplicationLifecycleObserver(ApplicationLifecycleUseCase applicationLifecycleUseCase) {
        this.applicationLifecycleUseCase = applicationLifecycleUseCase;
    }

    void onStart(@Observes StartupEvent event) {
        applicationLifecycleUseCase.initialize();
    }

    void onStop(@Observes ShutdownEvent event) {
        applicationLifecycleUseCase.shutdown();
    }
}
