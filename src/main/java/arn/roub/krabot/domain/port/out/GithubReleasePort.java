package arn.roub.krabot.domain.port.out;

import arn.roub.krabot.domain.model.ReleaseVersion;

/**
 * Port secondaire pour récupérer les releases GitHub.
 */
public interface GithubReleasePort {

    /**
     * Récupère la dernière version release depuis GitHub.
     *
     * @return la dernière version
     */
    ReleaseVersion getLatestRelease();
}
