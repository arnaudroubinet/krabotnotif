package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.model.Account;
import arn.roub.krabot.domain.port.in.CheckSleepUseCase;
import arn.roub.krabot.domain.port.out.KralandScrapingPort;
import arn.roub.krabot.domain.port.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implémentation du use case de vérification du rappel de sommeil.
 */
public class CheckSleepUseCaseImpl implements CheckSleepUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckSleepUseCaseImpl.class);
    private static final int MAX_RETRIES = 3;

    private final KralandScrapingPort kralandScrapingPort;
    private final NotificationPort notificationPort;
    private final Account account;

    public CheckSleepUseCaseImpl(
            KralandScrapingPort kralandScrapingPort,
            NotificationPort notificationPort,
            Account account
    ) {
        this.kralandScrapingPort = kralandScrapingPort;
        this.notificationPort = notificationPort;
        this.account = account;
    }

    @Override
    public void execute() {
        retryOnFailure(this::checkAndNotify);
    }

    private void checkAndNotify() {
        boolean sleepAvailable = kralandScrapingPort.isSleepAvailable(account);

        if (sleepAvailable) {
            LOGGER.info("Sleep action is available, sending reminder notification");
            notificationPort.sendSleepReminderNotification();
        } else {
            LOGGER.debug("Sleep action is not available");
        }
    }

    private void retryOnFailure(Runnable operation) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                operation.run();
                return;
            } catch (RuntimeException ex) {
                LOGGER.warn("Attempt {} failed: {}", attempt + 1, ex.getMessage());
                if (attempt == MAX_RETRIES - 1) {
                    throw ex;
                }
            }
        }
    }
}
