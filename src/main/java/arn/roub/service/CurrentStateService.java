package arn.roub.service;

import arn.roub.krabot.scrapper.CurrentState;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("krabot")
public class CurrentStateService {

    private final CurrentState currentState;

    public CurrentStateService(CurrentState currentState) {
        this.currentState = currentState;
    }

    @GET
    @Path("state")
    public CurrentStateDto getState() {
        return new CurrentStateDto(currentState.getNbkramail(), currentState.getHasNotification(), currentState.getCurrentVersion(), currentState.getLatestVersion());

    }
}
