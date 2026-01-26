package arn.roub.krabot.infrastructure.adapter.in.rest;

import arn.roub.krabot.domain.model.UserSummary;
import arn.roub.krabot.domain.port.in.UploadCharacteristicsUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.stream.Collectors;

/**
 * English-named resource for characteristics exchange (upload, listing, userscript).
 */
@Path("krabot/characteristics")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CharacteristicsExchangesResource {

    private final UploadCharacteristicsUseCase uploadUseCase;
    private final String backendUrl;

    public CharacteristicsExchangesResource(UploadCharacteristicsUseCase uploadUseCase,
                                            @ConfigProperty(name = "krabot.backend.url") String backendUrl) {
        this.uploadUseCase = uploadUseCase;
        this.backendUrl = backendUrl;
    }

    public record UploadCharacteristicsRequest(String playerId, String name, int pp) {}
    // now include pp directly in the user summary response
    public record UserSummaryResponse(String playerId, String name, int pp) {}

    @POST
    @Path("uploadCharacteristics")
    public Response uploadCharacteristics(@QueryParam("apiKey") String apiKey, UploadCharacteristicsRequest request) {
        // apiKey is mandatory
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("apiKey is required").build();
        }
        String namespace = apiKey.trim();
        if (request == null || request.playerId() == null || request.playerId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("playerId is required").build();
        }
        if (request.pp() < 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("pp must be >= 0").build();
        }
        uploadUseCase.upload(namespace, request.playerId(), request.name(), request.pp());
        return Response.ok().build();
    }

    @GET
    @Path("getUsers")
    public Response getUsers(@QueryParam("apiKey") String apiKey) {
        // apiKey is mandatory
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("apiKey is required").build();
        }
        String namespace = apiKey.trim();
        List<UserSummary> users = uploadUseCase.getUsers(namespace);
        // Map to response including PP — assume UserSummary exposes pp()
        List<UserSummaryResponse> resp = users.stream()
                .map(u -> new UserSummaryResponse(u.playerId(), u.name(), u.pp()))
                .collect(Collectors.toList());
        return Response.ok(resp).build();
    }

    @GET
    @Path("getUserScript.user.js")
    @Produces("application/javascript")
    public Response getUserScript() {
        // Load the userscript from resources to avoid complex Java string escaping
        try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("krabot-characteristics.user.js")) {
            if (in == null) return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("userscript not found").build();
            String content = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String hostWithProtocol = extractHost(backendUrl);
            // compute host-only for @connect (no http://). e.g. localhost:8080
            String hostOnly = hostWithProtocol.replaceFirst("^https?://", "").replaceAll("/.*$", "");
            String hostNoPort = hostOnly.replaceFirst(":\\d+$", "");
            content = content.replace("__BACKEND_URL__", backendUrl).replace("__BACKEND_HOST__", hostOnly).replace("__BACKEND_HOST_NO_PORT__", hostNoPort);
            return Response.ok(content).type("application/javascript").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("failed to load userscript").build();
        }
    }

    private String extractHost(String url) {
        try {
            java.net.URI u = java.net.URI.create(url);
            return u.getScheme() + "://" + u.getHost() + (u.getPort() != -1 ? ":" + u.getPort() : "");
        } catch (Exception e) {
            return url; // fallback to original if parsing fails
        }
    }
}
