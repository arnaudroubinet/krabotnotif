package arn.roub.krabot.utils.elements;

public record Footer(String text, String iconUrl) {

    public String getText() {
        return text;
    }

    public String getIconUrl() {
        return iconUrl;
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
