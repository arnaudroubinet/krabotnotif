package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.model.Account;
import arn.roub.krabot.domain.model.Kramail;
import arn.roub.krabot.domain.model.KramailId;
import arn.roub.krabot.domain.model.ScrapingResult;
import arn.roub.krabot.domain.port.in.CheckKramailsUseCase;
import arn.roub.krabot.domain.port.out.KralandScrapingPort;
import arn.roub.krabot.domain.port.out.NotificationPort;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;
import arn.roub.krabot.domain.service.NotificationDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Implémentation du use case de vérification des kramails.
 */
public class CheckKramailsUseCaseImpl implements CheckKramailsUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckKramailsUseCaseImpl.class);
    private static final int MAX_RETRIES = 3;

    private final KralandScrapingPort kralandScrapingPort;
    private final NotificationPort notificationPort;
    private final StateRepositoryPort stateRepositoryPort;
    private final NotificationDomainService notificationDomainService;
    private final Account account;

    public CheckKramailsUseCaseImpl(
            KralandScrapingPort kralandScrapingPort,
            NotificationPort notificationPort,
            StateRepositoryPort stateRepositoryPort,
            NotificationDomainService notificationDomainService,
            Account account
    ) {
        this.kralandScrapingPort = kralandScrapingPort;
        this.notificationPort = notificationPort;
        this.stateRepositoryPort = stateRepositoryPort;
        this.notificationDomainService = notificationDomainService;
        this.account = account;
    }

    @Override
    public void execute() {
        retryOnFailure(this::checkAndNotify);
    }

    private void checkAndNotify() {
        ScrapingResult result = kralandScrapingPort.scrape(account);

        processGeneralNotification(result);
        processKramailNotifications(result);
        updateState(result);
    }

    private void processGeneralNotification(ScrapingResult result) {
        if (result.hasNotification()) {
            if (notificationDomainService.shouldSendGeneralNotification(
                    result,
                    stateRepositoryPort.isGeneralNotificationAlreadySent()
            )) {
                notificationPort.sendGeneralNotification();
                stateRepositoryPort.markGeneralNotificationAsSent();
            }
            stateRepositoryPort.updateNotificationFlag(true);
        } else {
            stateRepositoryPort.updateNotificationFlag(false);
            stateRepositoryPort.resetGeneralNotificationFlag();
        }
    }

    private void processKramailNotifications(ScrapingResult result) {
        if (result.hasKramails()) {
            List<Kramail> kramailsToNotify = notificationDomainService.findKramailsToNotify(
                    result,
                    stateRepositoryPort::isKramailAlreadyNotified
            );

            for (Kramail kramail : kramailsToNotify) {
                notificationPort.sendKramailNotification(kramail);
                stateRepositoryPort.markKramailAsNotified(kramail.id());
            }

            Set<KramailId> currentIds = notificationDomainService.extractKramailIds(result);
            stateRepositoryPort.cleanupOldKramails(currentIds);
        } else {
            stateRepositoryPort.cleanupOldKramails(Set.of());
        }
    }

    private void updateState(ScrapingResult result) {
        stateRepositoryPort.updateKramailCount(result.kramails().size());
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
