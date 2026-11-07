package arn.roub.krabot.infrastructure.adapter.out.notification;

import arn.roub.krabot.domain.model.Kramail;
import arn.roub.krabot.domain.model.KramailId;
import arn.roub.krabot.domain.model.ReleaseVersion;
import arn.roub.krabot.shared.exception.NotificationException;
import arn.roub.krabot.shared.exception.RateLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DiscordNotificationAdapterTest {

    private DiscordWebhookClient client;
    private DiscordNotificationAdapter adapter;

    @BeforeEach
    void setUp() {
        client = mock(DiscordWebhookClient.class);
        adapter = new DiscordNotificationAdapter(
                client,
                "start",
                "stop",
                "From *originator* to *recipient*: *title*",
                "notif",
                "New release",
                "Oops"
        );
    }

    @Test
    void kramailNotification_replacesPlaceholders() {
        adapter.sendKramailNotification(new Kramail(new KramailId("1"), "Hi", "Alice", "Bob"));

        verify(client).send("From Alice to Bob: Hi");
    }

    @Test
    void kramailNotification_propagatesRateLimitSoItCanBeRetried() {
        doThrow(new RateLimitException("slow down")).when(client).send(anyString());

        assertThrows(RateLimitException.class,
                () -> adapter.sendKramailNotification(new Kramail(new KramailId("1"), "Hi", "Alice", "Bob")));
    }

    @Test
    void generalNotification_propagatesRateLimit() {
        doThrow(new RateLimitException("slow down")).when(client).send(anyString());

        assertThrows(RateLimitException.class, adapter::sendGeneralNotification);
    }

    @Test
    void generalNotification_sendsConfiguredMessage() {
        adapter.sendGeneralNotification();

        verify(client).send("notif");
    }

    @Test
    void lifecycleNotifications() {
        adapter.sendStartupNotification();
        adapter.sendShutdownNotification();

        verify(client).send("start");
        verify(client).send("stop");
    }

    @Test
    void releaseNotification_containsReleaseLink() {
        adapter.sendReleaseNotification(ReleaseVersion.of("v9.9.9"));

        verify(client).send("New release: https://github.com/arnaudroubinet/krabotnotif/releases/latest");
    }

    @Test
    void errorNotification_isPrefixed() {
        adapter.sendErrorNotification("boom");

        verify(client).send("Oops boom");
    }

    @Test
    void safeNotifications_neverThrow() {
        doThrow(new NotificationException("ko")).when(client).send(anyString());

        assertDoesNotThrow(adapter::sendStartupNotification);
        assertDoesNotThrow(adapter::sendShutdownNotification);
        assertDoesNotThrow(() -> adapter.sendErrorNotification("x"));
        assertDoesNotThrow(() -> adapter.sendReleaseNotification(ReleaseVersion.of("v1")));

        doThrow(new RateLimitException("slow")).when(client).send(anyString());
        assertDoesNotThrow(adapter::sendStartupNotification);
    }
}
