package arn.roub.krabot.scrapper;

public record ScrappingResponse(boolean hasKramail, boolean hasNotification) {

    public static ScrappingResponse of(boolean hasKramail, boolean hasNotification) {
        return new ScrappingResponse(hasKramail, hasNotification);
    }
}
