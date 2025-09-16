package arn.roub.krabot.utils.elements;

public final class Author {
    private final String name;
    private final String url;
    private final String iconUrl;

    public Author(String name, String url, String iconUrl) {
        this.name = name;
        this.url = url;
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    @Override
    public String toString() {
        return "Author{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author author)) return false;
        return java.util.Objects.equals(name, author.name) && 
               java.util.Objects.equals(url, author.url) && 
               java.util.Objects.equals(iconUrl, author.iconUrl);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, url, iconUrl);
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