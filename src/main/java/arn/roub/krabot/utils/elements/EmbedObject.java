package arn.roub.krabot.utils.elements;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList;

public class EmbedObject {
    private String title;
    private String description;
    private String url;
    private Color color;
    private Footer footer;
    private Thumbnail thumbnail;
    private Image image;
    private Author author;
    private final List<Field> fields;

    public EmbedObject(String title, String description, String url, Color color, 
                      Footer footer, Thumbnail thumbnail, Image image, Author author, 
                      List<Field> fields) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.color = color;
        this.footer = footer;
        this.thumbnail = thumbnail;
        this.image = image;
        this.author = author;
        this.fields = fields != null ? new ArrayList<>(fields) : new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public Color getColor() {
        return color;
    }

    public Footer getFooter() {
        return footer;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public Image getImage() {
        return image;
    }

    public Author getAuthor() {
        return author;
    }

    public List<Field> getFields() {
        return new ArrayList<>(fields);
    }

    public static EmbedObjectBuilder builder() {
        return new EmbedObjectBuilder();
    }

    public static class EmbedObjectBuilder {
        private String title;
        private String description;
        private String url;
        private Color color;
        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;
        private List<Field> fields = new ArrayList<>();

        public EmbedObjectBuilder title(String title) {
            this.title = title;
            return this;
        }

        public EmbedObjectBuilder description(String description) {
            this.description = description;
            return this;
        }

        public EmbedObjectBuilder url(String url) {
            this.url = url;
            return this;
        }

        public EmbedObjectBuilder color(Color color) {
            this.color = color;
            return this;
        }

        public EmbedObjectBuilder footer(Footer footer) {
            this.footer = footer;
            return this;
        }

        public EmbedObjectBuilder thumbnail(Thumbnail thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public EmbedObjectBuilder image(Image image) {
            this.image = image;
            return this;
        }

        public EmbedObjectBuilder author(Author author) {
            this.author = author;
            return this;
        }

        public EmbedObjectBuilder field(Field field) {
            this.fields.add(field);
            return this;
        }

        public EmbedObjectBuilder fields(List<Field> fields) {
            this.fields.clear();
            if (fields != null) {
                this.fields.addAll(fields);
            }
            return this;
        }

        public EmbedObject build() {
            return new EmbedObject(title, description, url, color, footer, thumbnail, image, author, fields);
        }
    }
}