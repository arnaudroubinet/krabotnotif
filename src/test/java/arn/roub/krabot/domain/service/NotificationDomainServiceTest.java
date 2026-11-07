package arn.roub.krabot.domain.service;

import arn.roub.krabot.domain.model.Kramail;
import arn.roub.krabot.domain.model.KramailId;
import arn.roub.krabot.domain.model.ScrapingResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NotificationDomainServiceTest {

    private final NotificationDomainService service = new NotificationDomainService();
    private final Kramail k1 = new Kramail(new KramailId("1"), "t1", "o1", "r");
    private final Kramail k2 = new Kramail(new KramailId("2"), "t2", "o2", "r");

    @Test
    void findKramailsToNotify_filtersAlreadyNotified() {
        ScrapingResult result = new ScrapingResult(List.of(k1, k2), false);

        List<Kramail> toNotify = service.findKramailsToNotify(result, id -> id.equals(k1.id()));

        assertEquals(List.of(k2), toNotify);
    }

    @Test
    void extractKramailIds_returnsAllIds() {
        assertEquals(Set.of(k1.id(), k2.id()),
                service.extractKramailIds(new ScrapingResult(List.of(k1, k2), false)));
        assertTrue(service.extractKramailIds(ScrapingResult.empty()).isEmpty());
    }

    @Test
    void shouldSendGeneralNotification_onlyWhenPresentAndNotAlreadySent() {
        ScrapingResult withNotif = new ScrapingResult(List.of(), true);
        assertTrue(service.shouldSendGeneralNotification(withNotif, false));
        assertFalse(service.shouldSendGeneralNotification(withNotif, true));
        assertFalse(service.shouldSendGeneralNotification(ScrapingResult.empty(), false));
    }
}
