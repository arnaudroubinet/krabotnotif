package arn.roub.krabot.scrapper;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Getter
@Setter
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
}
