package arn.roub.krabot.scrapper;

import java.util.List;

public record ScrappingResponse(List<Kramail> kramails, boolean hasNotification) {
}
