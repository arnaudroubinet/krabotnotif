package arn.roub.service;

import arn.roub.krabot.scrapper.CurrentState;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("krabot")
public class CurrentStateService {

    @GET
    @Path("state")
    public CurrentStateDto getState() {
        return new CurrentStateDto(CurrentState.nbkramail, CurrentState.hasNotification);

    }
}
