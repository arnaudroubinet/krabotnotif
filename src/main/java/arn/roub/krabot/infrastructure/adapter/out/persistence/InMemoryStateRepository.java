package arn.roub.krabot.infrastructure.adapter.out.persistence;

import arn.roub.krabot.domain.model.KramailId;
import arn.roub.krabot.domain.model.NotificationState;
import arn.roub.krabot.domain.model.ReleaseVersion;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Repository en mémoire pour l'état de l'application.
 */
@ApplicationScoped
public class InMemoryStateRepository implements StateRepositoryPort {

    private final AtomicInteger nbKramails = new AtomicInteger(0);
    private final AtomicBoolean hasNotification = new AtomicBoolean(false);
    private final ReleaseVersion currentVersion;
    private final AtomicReference<ReleaseVersion> latestVersion = new AtomicReference<>(ReleaseVersion.UNKNOWN);
    private final AtomicBoolean generalNotificationSent = new AtomicBoolean(false);
    private final ConcurrentHashMap<KramailId, Boolean> notifiedKramails = new ConcurrentHashMap<>();

    public InMemoryStateRepository(
            @ConfigProperty(name = "quarkus.application.version", defaultValue = "Unknown") String version
    ) {
        this.currentVersion = ReleaseVersion.of("v" + version);
    }

    @Override
    public NotificationState getState() {
        return new NotificationState(
                nbKramails.get(),
                hasNotification.get(),
                currentVersion,
                latestVersion.get()
        );
    }

    @Override
    public void updateKramailCount(int count) {
        nbKramails.set(count);
    }

    @Override
    public void updateNotificationFlag(boolean flag) {
        hasNotification.set(flag);
    }

    @Override
    public void updateLatestVersion(ReleaseVersion version) {
        latestVersion.set(version);
    }

    @Override
    public boolean isKramailAlreadyNotified(KramailId kramailId) {
        return notifiedKramails.containsKey(kramailId);
    }

    @Override
    public void markKramailAsNotified(KramailId kramailId) {
        notifiedKramails.put(kramailId, true);
    }

    @Override
    public void cleanupOldKramails(Set<KramailId> currentKramailIds) {
        notifiedKramails.keySet().removeIf(id -> !currentKramailIds.contains(id));
    }

    @Override
    public void resetGeneralNotificationFlag() {
        generalNotificationSent.set(false);
    }

    @Override
    public boolean isGeneralNotificationAlreadySent() {
        return generalNotificationSent.get();
    }

    @Override
    public void markGeneralNotificationAsSent() {
        generalNotificationSent.set(true);
    }

    @Override
    public void resetGeneralNotificationState() {
        generalNotificationSent.set(false);
    }

    @Override
    public void resetKramailsNotificationState() {
        notifiedKramails.clear();
    }
}
