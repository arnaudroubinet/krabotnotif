package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.model.Account;
import arn.roub.krabot.domain.model.Kramail;
import arn.roub.krabot.domain.model.KramailId;
import arn.roub.krabot.domain.model.ScrapingResult;
import arn.roub.krabot.domain.port.out.KralandScrapingPort;
import arn.roub.krabot.domain.port.out.NotificationPort;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;
import arn.roub.krabot.domain.service.NotificationDomainService;
import arn.roub.krabot.shared.exception.ScrapingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckKramailsUseCaseImplTest {

    private static final Account ACCOUNT = new Account("user", "pass");
    private static final Kramail K1 = new Kramail(new KramailId("1"), "Hello", "Alice", "Bob");
    private static final Kramail K2 = new Kramail(new KramailId("2"), "World", "Carol", "Bob");

    @Mock
    private KralandScrapingPort scrapingPort;
    @Mock
    private NotificationPort notificationPort;
    @Mock
    private StateRepositoryPort stateRepositoryPort;

    private CheckKramailsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CheckKramailsUseCaseImpl(
                scrapingPort, notificationPort, stateRepositoryPort, new NotificationDomainService(), ACCOUNT);
    }

    @Test
    void notifiesOnlyKramailsNotAlreadyNotified() {
        when(scrapingPort.scrape(ACCOUNT)).thenReturn(new ScrapingResult(List.of(K1, K2), false));
        when(stateRepositoryPort.isKramailAlreadyNotified(K1.id())).thenReturn(true);
        when(stateRepositoryPort.isKramailAlreadyNotified(K2.id())).thenReturn(false);

        useCase.execute();

        verify(notificationPort, never()).sendKramailNotification(K1);
        InOrder inOrder = inOrder(notificationPort, stateRepositoryPort);
        inOrder.verify(notificationPort).sendKramailNotification(K2);
        inOrder.verify(stateRepositoryPort).markKramailAsNotified(K2.id());
        verify(stateRepositoryPort).cleanupOldKramails(Set.of(K1.id(), K2.id()));
        verify(stateRepositoryPort).updateKramailCount(2);
    }

    @Test
    void noKramails_cleansUpEverythingAndSetsCountToZero() {
        when(scrapingPort.scrape(ACCOUNT)).thenReturn(ScrapingResult.empty());

        useCase.execute();

        verify(notificationPort, never()).sendKramailNotification(any());
        verify(stateRepositoryPort).cleanupOldKramails(Set.of());
        verify(stateRepositoryPort).updateKramailCount(0);
    }

    @Test
    void generalNotification_sentOnceWhenNotAlreadySent() {
        when(scrapingPort.scrape(ACCOUNT)).thenReturn(new ScrapingResult(List.of(), true));
        when(stateRepositoryPort.isGeneralNotificationAlreadySent()).thenReturn(false);

        useCase.execute();

        verify(notificationPort).sendGeneralNotification();
        verify(stateRepositoryPort).markGeneralNotificationAsSent();
        verify(stateRepositoryPort).updateNotificationFlag(true);
    }

    @Test
    void generalNotification_notResentWhenAlreadySent() {
        when(scrapingPort.scrape(ACCOUNT)).thenReturn(new ScrapingResult(List.of(), true));
        when(stateRepositoryPort.isGeneralNotificationAlreadySent()).thenReturn(true);

        useCase.execute();

        verify(notificationPort, never()).sendGeneralNotification();
        verify(stateRepositoryPort, never()).markGeneralNotificationAsSent();
        verify(stateRepositoryPort).updateNotificationFlag(true);
    }

    @Test
    void noGeneralNotification_resetsFlags() {
        when(scrapingPort.scrape(ACCOUNT)).thenReturn(ScrapingResult.empty());

        useCase.execute();

        verify(stateRepositoryPort).updateNotificationFlag(false);
        verify(stateRepositoryPort).resetGeneralNotificationFlag();
        verify(notificationPort, never()).sendGeneralNotification();
    }

    @Test
    void retriesThenSucceeds() {
        when(scrapingPort.scrape(ACCOUNT))
                .thenThrow(new ScrapingException("boom"))
                .thenReturn(ScrapingResult.empty());

        useCase.execute();

        verify(scrapingPort, times(2)).scrape(ACCOUNT);
        verify(stateRepositoryPort).updateKramailCount(0);
    }

    @Test
    void throwsAfterThreeFailedAttempts() {
        when(scrapingPort.scrape(ACCOUNT)).thenThrow(new ScrapingException("boom"));

        assertThrows(ScrapingException.class, useCase::execute);

        verify(scrapingPort, times(3)).scrape(ACCOUNT);
        verifyNoInteractions(notificationPort);
    }
}
