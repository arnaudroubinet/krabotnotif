package arn.roub.krabot.infrastructure.adapter.in.scheduler;

import arn.roub.krabot.application.service.NotificationOrchestrator;
import arn.roub.krabot.domain.port.in.CheckKramailsUseCase;
import arn.roub.krabot.domain.port.in.DelayKramailCheckUseCase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Timer pour la vérification périodique des kramails.
 * Permet de décaler dynamiquement la prochaine exécution.
 */
@ApplicationScoped
public class KramailCheckScheduler implements DelayKramailCheckUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(KramailCheckScheduler.class);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final CheckKramailsUseCase checkKramailsUseCase;
    private final NotificationOrchestrator notificationOrchestrator;
    private final Duration interval;
    private final Duration delayAmount;

    private ScheduledFuture<?> currentTask;
    private Instant nextExecutionTime;

    public KramailCheckScheduler(
            CheckKramailsUseCase checkKramailsUseCase,
            NotificationOrchestrator notificationOrchestrator,
            @ConfigProperty(name = "scheduler.kraland.scraping.every") Duration interval,
            @ConfigProperty(name = "scheduler.kraland.scraping.delay") Duration delayAmount
    ) {
        this.checkKramailsUseCase = checkKramailsUseCase;
        this.notificationOrchestrator = notificationOrchestrator;
        this.interval = interval;
        this.delayAmount = delayAmount;
    }

    @PostConstruct
    void start() {
        LOGGER.info("Starting kramail check timer with interval: {}", interval);
        scheduleNext(interval);
    }

    @PreDestroy
    void shutdown() {
        LOGGER.info("Shutting down kramail check timer");
        executor.shutdownNow();
    }

    private synchronized Instant scheduleNext(Duration delay) {
        currentTask = executor.schedule(this::executeAndReschedule, delay.toMillis(), TimeUnit.MILLISECONDS);
        nextExecutionTime = Instant.now().plus(delay);
        LOGGER.debug("Next kramail check scheduled at {}", nextExecutionTime);
        return nextExecutionTime;
    }

    private void executeAndReschedule() {
        try {
            checkKramailsUseCase.execute();
        } catch (RuntimeException e) {
            LOGGER.error("Error during kramail check: {}", e.getMessage());
            notificationOrchestrator.notifyError(e);
        }
        scheduleNext(interval);
    }

    @Override
    public synchronized Instant delay() {
        if (currentTask != null && !currentTask.isDone()) {
            long remainingMs = currentTask.getDelay(TimeUnit.MILLISECONDS);
            currentTask.cancel(false);
            Duration newDelay = Duration.ofMillis(remainingMs).plus(delayAmount);
            LOGGER.info("Delaying next kramail check by {}. New delay: {}", delayAmount, newDelay);
            return scheduleNext(newDelay);
        } else {
            LOGGER.warn("No scheduled task to delay");
            return nextExecutionTime;
        }
    }
}
