package arn.roub.hook.scrapper;

public class ScrappingResponse {
    private final boolean hasKramail;
    private final boolean hasNotification;

    private ScrappingResponse(boolean hasKramail, boolean hasNotification) {
        this.hasKramail = hasKramail;
        this.hasNotification = hasNotification;
    }

    public static ScrappingResponse of(boolean hasKramail, boolean hasNotification) {
        return new ScrappingResponse(hasKramail, hasNotification);
    }

    public boolean hasKramail() {
        return hasKramail;
    }

    public boolean hasNotification() {
        return hasNotification;
    }
}
