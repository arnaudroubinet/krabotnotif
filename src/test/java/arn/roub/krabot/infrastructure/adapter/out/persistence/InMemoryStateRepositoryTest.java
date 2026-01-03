package arn.roub.krabot.infrastructure.adapter.out.persistence;

import arn.roub.krabot.domain.model.KramailId;
import arn.roub.krabot.domain.model.NotificationState;
import arn.roub.krabot.domain.model.ReleaseVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryStateRepository.
 * Tests the state management that tracks application state.
 */
@DisplayName("InMemoryStateRepository Tests")
class InMemoryStateRepositoryTest {

    private InMemoryStateRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryStateRepository("1.0.0");
    }

    @Test
    @DisplayName("Should initialize with provided version")
    void shouldInitializeWithVersion() {
        // When
        NotificationState state = repository.getState();

        // Then
        assertNotNull(state);
        assertEquals("v1.0.0", state.currentVersion().tag());
        assertEquals(0, state.nbKramails());
        assertFalse(state.hasNotification());
    }

    @Test
    @DisplayName("Should update krammail count")
    void shouldUpdateKramailCount() {
        // When
        repository.updateKramailCount(5);

        // Then
        assertEquals(5, repository.getState().nbKramails());

        // When
        repository.updateKramailCount(2);

        // Then
        assertEquals(2, repository.getState().nbKramails());
    }

    @Test
    @DisplayName("Should update notification flag")
    void shouldUpdateNotificationFlag() {
        // When
        repository.updateNotificationFlag(true);

        // Then
        assertTrue(repository.getState().hasNotification());

        // When
        repository.updateNotificationFlag(false);

        // Then
        assertFalse(repository.getState().hasNotification());
    }

    @Test
    @DisplayName("Should update latest version")
    void shouldUpdateLatestVersion() {
        // Given
        ReleaseVersion version = ReleaseVersion.of("v2.0.0");

        // When
        repository.updateLatestVersion(version);

        // Then
        assertEquals("v2.0.0", repository.getState().latestVersion().tag());
    }

    @Test
    @DisplayName("Should track notified kramails")
    void shouldTrackNotifiedKramails() {
        // Given
        KramailId id = new KramailId("km1");

        // Initially not notified
        assertFalse(repository.isKramailAlreadyNotified(id));

        // When
        repository.markKramailAsNotified(id);

        // Then
        assertTrue(repository.isKramailAlreadyNotified(id));
    }

    @Test
    @DisplayName("Should cleanup old kramails")
    void shouldCleanupOldKramails() {
        // Given - Mark some kramails as notified
        KramailId id1 = new KramailId("1");
        KramailId id2 = new KramailId("2");
        KramailId id3 = new KramailId("3");

        repository.markKramailAsNotified(id1);
        repository.markKramailAsNotified(id2);
        repository.markKramailAsNotified(id3);

        // When - Cleanup keeping only id1 and id3
        repository.cleanupOldKramails(Set.of(id1, id3));

        // Then - id2 should be removed
        assertTrue(repository.isKramailAlreadyNotified(id1));
        assertFalse(repository.isKramailAlreadyNotified(id2));
        assertTrue(repository.isKramailAlreadyNotified(id3));
    }

    @Test
    @DisplayName("Should track general notification flag")
    void shouldTrackGeneralNotificationFlag() {
        // Initially not sent
        assertFalse(repository.isGeneralNotificationAlreadySent());

        // When
        repository.markGeneralNotificationAsSent();

        // Then
        assertTrue(repository.isGeneralNotificationAlreadySent());

        // When reset
        repository.resetGeneralNotificationFlag();

        // Then
        assertFalse(repository.isGeneralNotificationAlreadySent());
    }

    @Test
    @DisplayName("Should maintain independent state values")
    void shouldMaintainIndependentState() {
        // When
        repository.updateNotificationFlag(true);
        repository.updateKramailCount(3);
        repository.updateLatestVersion(ReleaseVersion.of("v2.0.0"));

        // Then
        NotificationState state = repository.getState();
        assertTrue(state.hasNotification());
        assertEquals(3, state.nbKramails());
        assertEquals("v2.0.0", state.latestVersion().tag());

        // When - Change one value
        repository.updateNotificationFlag(false);

        // Then - Other values unchanged
        NotificationState newState = repository.getState();
        assertFalse(newState.hasNotification());
        assertEquals(3, newState.nbKramails());
        assertEquals("v2.0.0", newState.latestVersion().tag());
    }
}
