package arn.roub.krabot.infrastructure.adapter.in.scheduler;

import arn.roub.krabot.domain.port.in.ReportErrorUseCase;
import arn.roub.krabot.domain.port.in.CheckReleaseUseCase;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler pour la vérification périodique des releases GitHub.
 */
@ApplicationScoped
public class ReleaseCheckScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseCheckScheduler.class);

    private final CheckReleaseUseCase checkReleaseUseCase;
    private final ReportErrorUseCase reportErrorUseCase;

    public ReleaseCheckScheduler(
            CheckReleaseUseCase checkReleaseUseCase,
            ReportErrorUseCase reportErrorUseCase
    ) {
        this.checkReleaseUseCase = checkReleaseUseCase;
        this.reportErrorUseCase = reportErrorUseCase;
    }

    @Scheduled(cron = "${scheduler.github.scraping.cron}")
    public void execute() {
        try {
            checkReleaseUseCase.execute();
        } catch (RuntimeException e) {
            LOGGER.error("Error during release check: {}", e.getMessage());
            reportErrorUseCase.notifyError(e);
        }
    }
}
