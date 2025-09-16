package arn.roub.krabot.scrapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CurrentState {

    public CurrentState(@ConfigProperty(name = "quarkus.application.version", defaultValue = "Unknown") String version) {
        this.currentVersion = "v"+version;
        this.nbkramail = 0;
        this.hasNotification = false;
        this.latestVersion = "Unknown";
    }

    private Integer nbkramail;
    private Boolean hasNotification;
    private String currentVersion;
    private String latestVersion;

    public Integer getNbkramail() {
        return nbkramail;
    }

    public void setNbkramail(Integer nbkramail) {
        this.nbkramail = nbkramail;
    }

    public Boolean getHasNotification() {
        return hasNotification;
    }

    public void setHasNotification(Boolean hasNotification) {
        this.hasNotification = hasNotification;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }
}
