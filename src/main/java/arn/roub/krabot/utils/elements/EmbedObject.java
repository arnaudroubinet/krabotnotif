package arn.roub.krabot.utils.elements;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.awt.Color;
import java.util.List;

@Getter
@Builder
public class EmbedObject {
    private String title;
    private String description;
    private String url;
    private Color color;

    private Footer footer;
    private Thumbnail thumbnail;
    private Image image;
    private Author author;
    @Singular
    private final List<Field> fields;
}