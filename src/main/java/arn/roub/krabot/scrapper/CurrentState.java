package arn.roub.krabot.scrapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@ApplicationScoped
public class CurrentState {

    private int nbkramail;
    private boolean hasNotification;
    private final String currentVersion;
    private String latestVersion;

    public CurrentState(@ConfigProperty(name = "quarkus.application.version", defaultValue = "Unknown") String version) {
        String gitVersion = loadGitVersion();
        this.currentVersion = gitVersion != null ? gitVersion : "v" + version;
        this.nbkramail = 0;
        this.hasNotification = false;
        this.latestVersion = "Unknown";
    }

    private String loadGitVersion() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("git.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                String describe = prop.getProperty("git.commit.id.describe");
                if (describe != null && !describe.isEmpty()) {
                    // git.commit.id.describe gives us something like "v2.0.4" or "v2.0.4-5-gabcdef" or "5d446c0-dirty"
                    // We want to extract the tag part if it exists
                    String[] parts = describe.split("-");
                    if (parts.length > 0 && parts[0].startsWith("v")) {
                        // First part is a version tag like "v2.0.4"
                        return parts[0];
                    }
                    // If no tag found, try to use the abbreviated commit ID
                    String abbrev = prop.getProperty("git.commit.id.abbrev");
                    if (abbrev != null && !abbrev.isEmpty()) {
                        return abbrev;
                    }
                }
            }
        } catch (IOException e) {
            // If git.properties doesn't exist or can't be read, return null to use fallback
        }
        return null;
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
