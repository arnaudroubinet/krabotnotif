package arn.roub.krabot.infrastructure.adapter.in.scheduler;

import arn.roub.krabot.application.service.NotificationOrchestrator;
import arn.roub.krabot.domain.port.in.CheckSleepUseCase;
import arn.roub.krabot.domain.port.in.DelaySleepCheckUseCase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Timer pour la vérification quotidienne du rappel de sommeil.
 * Permet de décaler dynamiquement la prochaine exécution.
 */
@ApplicationScoped
public class SleepCheckScheduler implements DelaySleepCheckUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SleepCheckScheduler.class);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final CheckSleepUseCase checkSleepUseCase;
    private final NotificationOrchestrator notificationOrchestrator;
    private final LocalTime scheduledTime;
    private final Duration delayAmount;

    private ScheduledFuture<?> currentTask;
    private Instant nextExecutionTime;

    public SleepCheckScheduler(
            CheckSleepUseCase checkSleepUseCase,
            NotificationOrchestrator notificationOrchestrator,
            @ConfigProperty(name = "scheduler.sleep.time", defaultValue = "20:00") String scheduledTimeStr,
            @ConfigProperty(name = "scheduler.kraland.scraping.delay") Duration delayAmount
    ) {
        this.checkSleepUseCase = checkSleepUseCase;
        this.notificationOrchestrator = notificationOrchestrator;
        this.scheduledTime = LocalTime.parse(scheduledTimeStr);
        this.delayAmount = delayAmount;
    }

    @PostConstruct
    void start() {
        LOGGER.info("Starting sleep check timer scheduled at {}", scheduledTime);
        scheduleNextExecution();
    }

    @PreDestroy
    void shutdown() {
        LOGGER.info("Shutting down sleep check timer");
        executor.shutdownNow();
    }

    private synchronized Instant scheduleNextExecution() {
        Duration delay = calculateDelayUntilNextExecution();
        return scheduleNext(delay);
    }

    private Duration calculateDelayUntilNextExecution() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextExecution = now.with(scheduledTime);

        if (now.isAfter(nextExecution)) {
            nextExecution = nextExecution.plusDays(1);
        }

        return Duration.between(now, nextExecution);
    }

    private synchronized Instant scheduleNext(Duration delay) {
        currentTask = executor.schedule(this::executeAndReschedule, delay.toMillis(), TimeUnit.MILLISECONDS);
        nextExecutionTime = Instant.now().plus(delay);
        LOGGER.debug("Next sleep check scheduled at {}", nextExecutionTime);
        return nextExecutionTime;
    }

    private void executeAndReschedule() {
        try {
            checkSleepUseCase.execute();
        } catch (RuntimeException e) {
            LOGGER.error("Error during sleep check: {}", e.getMessage());
            notificationOrchestrator.notifyError(e);
        }
        scheduleNextExecution();
    }

    @Override
    public synchronized Instant delay() {
        if (currentTask != null && !currentTask.isDone()) {
            long remainingMs = currentTask.getDelay(TimeUnit.MILLISECONDS);
            currentTask.cancel(false);
            Duration newDelay = Duration.ofMillis(remainingMs).plus(delayAmount);
            LOGGER.info("Delaying next sleep check by {}. New delay: {}", delayAmount, newDelay);
            return scheduleNext(newDelay);
        } else {
            LOGGER.warn("No scheduled task to delay");
            return nextExecutionTime;
        }
    }
}
