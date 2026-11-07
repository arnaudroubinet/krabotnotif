package arn.roub.krabot.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void releaseVersion_rejectsBlankTag(String tag) {
        assertThrows(IllegalArgumentException.class, () -> ReleaseVersion.of(tag));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void kramailId_rejectsBlankValue(String value) {
        assertThrows(IllegalArgumentException.class, () -> new KramailId(value));
    }

    @Test
    void account_rejectsBlankCredentials() {
        assertThrows(IllegalArgumentException.class, () -> new Account(null, "p"));
        assertThrows(IllegalArgumentException.class, () -> new Account("u", " "));
        assertDoesNotThrow(() -> new Account("u", "p"));
    }

    @Test
    void releaseVersion_isNewerThanWhenTagDiffers() {
        assertTrue(ReleaseVersion.of("v2").isNewerThan(ReleaseVersion.of("v1")));
        assertFalse(ReleaseVersion.of("v1").isNewerThan(ReleaseVersion.of("v1")));
    }

    @Test
    void notificationState_initialAndWithers() {
        NotificationState state = NotificationState.initial(ReleaseVersion.of("v1"));
        assertEquals(0, state.nbKramails());
        assertFalse(state.hasNotification());
        assertEquals(ReleaseVersion.UNKNOWN, state.latestVersion());
        assertFalse(state.hasNewRelease(), "UNKNOWN latest version is never a new release");

        NotificationState updated = state.withKramailCount(4).withNotificationFlag(true)
                .withLatestVersion(ReleaseVersion.of("v2"));
        assertEquals(4, updated.nbKramails());
        assertTrue(updated.hasNotification());
        assertTrue(updated.hasNewRelease());
        assertFalse(state.withLatestVersion(ReleaseVersion.of("v1")).hasNewRelease());
    }

    @Test
    void characteristic_convenienceConstructorSetsTimestamp() {
        assertNotNull(new Characteristic("1", "n", 2).updatedAt());
    }
}
