package arn.roub.krabot.utils.elements;

public record Image(String url) {

    public String getUrl() {
        return url;
    }

    public static ImageBuilder builder() {
        return new ImageBuilder();
    }

    public static class ImageBuilder {
        private String url;

        public ImageBuilder url(String url) {
            this.url = url;
            return this;
        }

        public Image build() {
            return new Image(url);
        }
    }
}