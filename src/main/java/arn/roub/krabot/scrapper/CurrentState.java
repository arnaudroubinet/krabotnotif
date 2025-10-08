package arn.roub.krabot.scrapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class CurrentState {

    private final AtomicInteger nbkramail = new AtomicInteger(0);
    private final AtomicReference<Boolean> hasNotification = new AtomicReference<>(false);
    private final String currentVersion;
    private final AtomicReference<String> latestVersion = new AtomicReference<>("Unknown");

    public CurrentState(@ConfigProperty(name = "quarkus.application.version", defaultValue = "Unknown") String version) {
        this.currentVersion = "v" + version;
    }

    public Integer getNbkramail() {
        return nbkramail.get();
    }

    public void setNbkramail(Integer value) {
        this.nbkramail.set(value);
    }

    public Boolean getHasNotification() {
        return hasNotification.get();
    }

    public void setHasNotification(Boolean value) {
        this.hasNotification.set(value);
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestVersion() {
        return latestVersion.get();
    }

    public void setLatestVersion(String value) {
        this.latestVersion.set(value);
    }
}
