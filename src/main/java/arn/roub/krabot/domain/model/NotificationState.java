package arn.roub.krabot.domain.model;

/**
 * Value Object représentant l'état actuel des notifications.
 */
public record NotificationState(
        int nbKramails,
        boolean hasNotification,
        ReleaseVersion currentVersion,
        ReleaseVersion latestVersion
) {

    public static NotificationState initial(ReleaseVersion currentVersion) {
        return new NotificationState(0, false, currentVersion, ReleaseVersion.UNKNOWN);
    }

    public NotificationState withKramailCount(int count) {
        return new NotificationState(count, hasNotification, currentVersion, latestVersion);
    }

    public NotificationState withNotificationFlag(boolean hasNotification) {
        return new NotificationState(nbKramails, hasNotification, currentVersion, latestVersion);
    }

    public NotificationState withLatestVersion(ReleaseVersion version) {
        return new NotificationState(nbKramails, hasNotification, currentVersion, version);
    }

    public boolean hasNewRelease() {
        return !latestVersion.equals(ReleaseVersion.UNKNOWN)
                && latestVersion.isNewerThan(currentVersion);
    }
}
