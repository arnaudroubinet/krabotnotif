package arn.roub.krabot.scheduler.job;

import arn.roub.krabot.errors.ExceptionNotificationService;
import arn.roub.krabot.scrapper.ScrappingService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@AllArgsConstructor
public class ScrapAndNotifyJob {

    private final ScrappingService scrappingService;

    private final ExceptionNotificationService exceptionNotificationService;

    private final Logger LOGGER = LoggerFactory.getLogger(ScrapAndNotifyJob.class);

    @Scheduled(every = "{scheduler.kraland.scraping.every}")
    public void execute() {
        try {
            scrappingService.loadKiAndSendNotificationIfWeHaveReport();
        } catch (RuntimeException runtimeException) {
            try {
                LOGGER.error("Error occur during the scrap or the notification post.", runtimeException);
                exceptionNotificationService.exceptionManagement(runtimeException);
            } catch (RuntimeException exceptionManagementError) {
                LOGGER.error("Error occur during the error notification !!", exceptionManagementError);
                runtimeException.addSuppressed(exceptionManagementError);
            }
        }
    }

}
