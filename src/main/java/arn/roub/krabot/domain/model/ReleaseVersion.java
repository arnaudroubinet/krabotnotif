package arn.roub.krabot.domain.model;

/**
 * Value Object représentant une version de release.
 */
public record ReleaseVersion(String tag) {

    public static final ReleaseVersion UNKNOWN = new ReleaseVersion("Unknown");

    public ReleaseVersion {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("Release tag cannot be null or blank");
        }
    }

    public boolean isNewerThan(ReleaseVersion other) {
        return !this.tag.equals(other.tag);
    }

    public static ReleaseVersion of(String tag) {
        return new ReleaseVersion(tag);
    }
}
