package arn.roub.krabot.utils.elements;

public final class Footer {
    private final String text;
    private final String iconUrl;

    public Footer(String text, String iconUrl) {
        this.text = text;
        this.iconUrl = iconUrl;
    }

    public String getText() {
        return text;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    @Override
    public String toString() {
        return "Footer{" +
                "text='" + text + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Footer footer)) return false;
        return java.util.Objects.equals(text, footer.text) && 
               java.util.Objects.equals(iconUrl, footer.iconUrl);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(text, iconUrl);
    }

    public static FooterBuilder builder() {
        return new FooterBuilder();
    }

    public static class FooterBuilder {
        private String text;
        private String iconUrl;

        public FooterBuilder text(String text) {
            this.text = text;
            return this;
        }

        public FooterBuilder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public Footer build() {
            return new Footer(text, iconUrl);
        }
    }
}
