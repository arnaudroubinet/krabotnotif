package arn.roub.krabot.utils.elements;

public record Thumbnail(String url) {

    public String getUrl() {
        return url;
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