package arn.roub.krabot.utils.elements;

public record Field(String name, String value, boolean inline) {

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isInline() {
        return inline;
    }

    public static FieldBuilder builder() {
        return new FieldBuilder();
    }

    public static class FieldBuilder {
        private String name;
        private String value;
        private boolean inline;

        public FieldBuilder name(String name) {
            this.name = name;
            return this;
        }

        public FieldBuilder value(String value) {
            this.value = value;
            return this;
        }

        public FieldBuilder inline(boolean inline) {
            this.inline = inline;
            return this;
        }

        public Field build() {
            return new Field(name, value, inline);
        }
    }
}