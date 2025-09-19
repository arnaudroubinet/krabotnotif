package arn.roub.krabot.utils.elements;

public record Author(String name, String url, String iconUrl) {

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public static AuthorBuilder builder() {
        return new AuthorBuilder();
    }

    public static class AuthorBuilder {
        private String name;
        private String url;
        private String iconUrl;

        public AuthorBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AuthorBuilder url(String url) {
            this.url = url;
            return this;
        }

        public AuthorBuilder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public Author build() {
            return new Author(name, url, iconUrl);
        }
    }
}