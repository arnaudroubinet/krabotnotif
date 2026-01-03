package arn.roub.krabot.infrastructure.adapter.in.rest;

import arn.roub.krabot.domain.model.NotificationState;
import arn.roub.krabot.domain.port.in.GetCurrentStateUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Adapter REST pour exposer l'état courant.
 */
@Path("krabot")
@ApplicationScoped
public class StateRestAdapter {

    private final GetCurrentStateUseCase getCurrentStateUseCase;

    public StateRestAdapter(GetCurrentStateUseCase getCurrentStateUseCase) {
        this.getCurrentStateUseCase = getCurrentStateUseCase;
    }

    @GET
    @Path("state")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getState() {
        NotificationState state = getCurrentStateUseCase.execute();
        StateDto dto = new StateDto(
                state.nbKramails(),
                state.hasNotification(),
                state.currentVersion().tag(),
                state.latestVersion().tag()
        );
        return Response.ok(dto).build();
    }

    /**
     * DTO pour l'API REST.
     */
    public record StateDto(
            int nbKramail,
            boolean hasNotification,
            String currentVersion,
            String latestVersion
    ) {}
}
