package arn.roub.krabot.scheduler.job;

import arn.roub.krabot.errors.ExceptionNotificationService;
import arn.roub.krabot.scrapper.ScrappingService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CheckReleaseAndNotifyJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckReleaseAndNotifyJob.class);
    private final ScrappingService scrappingService;
    private final ExceptionNotificationService exceptionNotificationService;

    public CheckReleaseAndNotifyJob(ScrappingService scrappingService, ExceptionNotificationService exceptionNotificationService) {
        this.scrappingService = scrappingService;
        this.exceptionNotificationService = exceptionNotificationService;
    }

    @Scheduled(cron = "{scheduler.github.scraping.cron}")
    public void execute() {
        try {
            scrappingService.checkGithubReleaseAndNotify();
        } catch (RuntimeException runtimeException) {
            try {
                LOGGER.error("Error occurred during the scrap or the notification post. {}", runtimeException.getMessage());
                exceptionNotificationService.exceptionManagement(runtimeException);
            } catch (RuntimeException exceptionManagementError) {
                LOGGER.error("Error occurred during the error notification !! {}", exceptionManagementError.getMessage());
                runtimeException.addSuppressed(exceptionManagementError);
            }
        }
    }

}
