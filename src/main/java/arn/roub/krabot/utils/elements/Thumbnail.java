package arn.roub.krabot.utils.elements;

public final class Thumbnail {
    private final String url;

    public Thumbnail(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Thumbnail{" +
                "url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Thumbnail thumbnail)) return false;
        return java.util.Objects.equals(url, thumbnail.url);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(url);
    }

    public static ThumbnailBuilder builder() {
        return new ThumbnailBuilder();
    }

    public static class ThumbnailBuilder {
        private String url;

        public ThumbnailBuilder url(String url) {
            this.url = url;
            return this;
        }

        public Thumbnail build() {
            return new Thumbnail(url);
        }
    }
}