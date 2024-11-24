package arn.roub.krabot.scrapper;

import java.util.List;

public record ScrappingResponse(List<Kramail> kramails, boolean hasNotification) {

    public static ScrappingResponse of(List<Kramail> kramails, boolean hasNotification) {
        return new ScrappingResponse(kramails, hasNotification);
    }
}
