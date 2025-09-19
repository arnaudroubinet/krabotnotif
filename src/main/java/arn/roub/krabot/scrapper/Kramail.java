package arn.roub.krabot.scrapper;

public record Kramail(String id, String title, String originator) {

    public static KramailBuilder builder() {
        return new KramailBuilder();
    }

    public static class KramailBuilder {
        private String id;
        private String title;
        private String originator;

        public KramailBuilder id(String id) {
            this.id = id;
            return this;
        }

        public KramailBuilder title(String title) {
            this.title = title;
            return this;
        }

        public KramailBuilder originator(String originator) {
            this.originator = originator;
            return this;
        }

        public Kramail build() {
            return new Kramail(id, title, originator);
        }
    }
}
