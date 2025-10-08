package arn.roub.service;

import arn.roub.krabot.scrapper.CurrentState;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("krabot")
public class CurrentStateService {

    private final CurrentState currentState;

    public CurrentStateService(CurrentState currentState) {
        this.currentState = currentState;
    }

    @GET
    @Path("state")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getState() {
        CurrentStateDto dto = new CurrentStateDto(
                currentState.getNbkramail() != null ? currentState.getNbkramail() : 0,
                currentState.getHasNotification() != null ? currentState.getHasNotification() : false,
                currentState.getCurrentVersion(),
                currentState.getLatestVersion()
        );
        return Response.ok(dto).build();
    }
}
