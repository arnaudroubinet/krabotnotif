package arn.roub.hook.quartz.job;

import arn.roub.hook.errors.ExceptionNotificationService;
import arn.roub.hook.scrapper.ScrappingService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScrapAndNotifyJob implements Job {

    private final ScrappingService scrappingService;

    private final ExceptionNotificationService exceptionNotificationService;

    private final Logger LOGGER = LoggerFactory.getLogger(ScrapAndNotifyJob.class);

    public ScrapAndNotifyJob(ScrappingService scrappingService, ExceptionNotificationService exceptionNotificationService) {
        this.scrappingService = scrappingService;
        this.exceptionNotificationService = exceptionNotificationService;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
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
            throw new JobExecutionException(runtimeException);
        }
    }

}
