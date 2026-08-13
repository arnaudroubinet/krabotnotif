package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.model.Account;
import arn.roub.krabot.domain.port.in.CheckSleepUseCase;
import arn.roub.krabot.domain.port.out.KralandScrapingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implémentation du use case de rappel de sommeil : passe l'ordre "Dormir" automatiquement
 * si l'action est disponible.
 */
public class CheckSleepUseCaseImpl implements CheckSleepUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckSleepUseCaseImpl.class);
    private static final int MAX_RETRIES = 3;

    private final KralandScrapingPort kralandScrapingPort;
    private final Account account;

    public CheckSleepUseCaseImpl(
            KralandScrapingPort kralandScrapingPort,
            Account account
    ) {
        this.kralandScrapingPort = kralandScrapingPort;
        this.account = account;
    }

    @Override
    public void execute() {
        retryOnFailure(this::checkAndSleep);
    }

    private void checkAndSleep() {
        LOGGER.info("Checking if sleep action is available...");
        boolean slept = kralandScrapingPort.sleepIfAvailable(account);

        if (slept) {
            LOGGER.info("Sleep order submitted");
        } else {
            LOGGER.info("Sleep action is not available (already done today)");
        }
    }

    private void retryOnFailure(Runnable operation) {
        for (int attempt = 0; true; attempt++) {
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
