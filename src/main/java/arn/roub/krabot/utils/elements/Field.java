package arn.roub.krabot.utils.elements;

public final class Field {
    private final String name;
    private final String value;
    private final boolean inline;

    public Field(String name, String value, boolean inline) {
        this.name = name;
        this.value = value;
        this.inline = inline;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isInline() {
        return inline;
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", inline=" + inline +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Field field)) return false;
        return inline == field.inline && 
               java.util.Objects.equals(name, field.name) && 
               java.util.Objects.equals(value, field.value);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, value, inline);
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