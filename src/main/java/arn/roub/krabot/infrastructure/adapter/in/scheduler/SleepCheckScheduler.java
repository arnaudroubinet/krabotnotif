package arn.roub.krabot.infrastructure.adapter.in.scheduler;

import arn.roub.krabot.application.service.NotificationOrchestrator;
import arn.roub.krabot.domain.port.in.CheckSleepUseCase;
import io.quarkus.runtime.Startup;
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
import java.util.concurrent.TimeUnit;

/**
 * Timer pour la vérification quotidienne du rappel de sommeil.
 * S'exécute strictement à l'heure programmée, sans jamais être décalé par l'activité du joueur.
 */
@Startup
@ApplicationScoped
public class SleepCheckScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SleepCheckScheduler.class);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final CheckSleepUseCase checkSleepUseCase;
    private final NotificationOrchestrator notificationOrchestrator;
    private final LocalTime scheduledTime;

    private long scheduleGeneration = 0;

    public SleepCheckScheduler(
            CheckSleepUseCase checkSleepUseCase,
            NotificationOrchestrator notificationOrchestrator,
            @ConfigProperty(name = "scheduler.sleep.time", defaultValue = "23:55") String scheduledTimeStr
    ) {
        this.checkSleepUseCase = checkSleepUseCase;
        this.notificationOrchestrator = notificationOrchestrator;
        this.scheduledTime = LocalTime.parse(scheduledTimeStr);
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

    private synchronized void scheduleNextExecution() {
        Duration delay = calculateDelayUntilNextExecution();
        scheduleNext(delay);
    }

    private Duration calculateDelayUntilNextExecution() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextExecution = now.with(scheduledTime);

        if (now.isAfter(nextExecution)) {
            nextExecution = nextExecution.plusDays(1);
        }

        return Duration.between(now, nextExecution);
    }

    private synchronized void scheduleNext(Duration delay) {
        long gen = ++scheduleGeneration;
        executor.schedule(() -> executeAndReschedule(gen), delay.toMillis(), TimeUnit.MILLISECONDS);
        Instant nextExecutionTime = Instant.now().plus(delay);
        LOGGER.info("Next sleep check scheduled at {} (in {}, generation {})", nextExecutionTime, delay, gen);
    }

    private void executeAndReschedule(long generation) {
        LOGGER.info("Executing scheduled sleep check...");
        try {
            checkSleepUseCase.execute();
        } catch (RuntimeException e) {
            LOGGER.error("Error during sleep check: {}", e.getMessage());
            notificationOrchestrator.notifyError(e);
        }
        rescheduleIfCurrent(generation);
    }

    private synchronized void rescheduleIfCurrent(long generation) {
        if (generation == scheduleGeneration) {
            scheduleNextExecution();
        } else {
            LOGGER.debug("Skipping reschedule: generation {} is stale (current: {})", generation, scheduleGeneration);
        }
    }
}
