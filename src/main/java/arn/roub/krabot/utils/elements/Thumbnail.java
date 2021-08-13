package arn.roub.krabot.utils.elements;


import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
@Getter
@Builder
@ToString
@EqualsAndHashCode
public final class Thumbnail {
    private final String url;
}