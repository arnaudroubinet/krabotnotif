package arn.roub.krabot.scrapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CurrentState {

    private int nbkramail;
    private boolean hasNotification;
    private final String currentVersion;
    private String latestVersion;

    public CurrentState(@ConfigProperty(name = "quarkus.application.version", defaultValue = "Unknown") String version) {
        this.currentVersion = "v"+version;
        this.nbkramail = 0;
        this.hasNotification = false;
        this.latestVersion = "Unknown";
    }

    public int getNbkramail() {
        return nbkramail;
    }

    public void setNbkramail(int nbkramail) {
        this.nbkramail = nbkramail;
    }

    public boolean getHasNotification() {
        return hasNotification;
    }

    public void setHasNotification(boolean hasNotification) {
        this.hasNotification = hasNotification;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }
}
