package arn.roub.krabot.infrastructure.adapter.in.scheduler;

import arn.roub.krabot.application.service.NotificationOrchestrator;
import arn.roub.krabot.domain.port.in.CheckKramailsUseCase;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler pour la vérification périodique des kramails.
 */
@ApplicationScoped
public class KramailCheckScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KramailCheckScheduler.class);

    private final CheckKramailsUseCase checkKramailsUseCase;
    private final NotificationOrchestrator notificationOrchestrator;

    public KramailCheckScheduler(
            CheckKramailsUseCase checkKramailsUseCase,
            NotificationOrchestrator notificationOrchestrator
    ) {
        this.checkKramailsUseCase = checkKramailsUseCase;
        this.notificationOrchestrator = notificationOrchestrator;
    }

    @Scheduled(every = "${scheduler.kraland.scraping.every}")
    public void execute() {
        try {
            checkKramailsUseCase.execute();
        } catch (RuntimeException e) {
            LOGGER.error("Error during kramail check: {}", e.getMessage());
            notificationOrchestrator.notifyError(e);
        }
    }
}
