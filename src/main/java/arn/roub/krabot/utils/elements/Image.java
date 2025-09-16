package arn.roub.krabot.utils.elements;

public final class Image {
    private final String url;

    public Image(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Image{" +
                "url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Image image)) return false;
        return java.util.Objects.equals(url, image.url);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(url);
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