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

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
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
    private final String quarkusAppVersion; // may be empty

    public CharacteristicsExchangesResource(UploadCharacteristicsUseCase uploadUseCase,
                                            @ConfigProperty(name = "krabot.backend.url") String backendUrl,
                                            @ConfigProperty(name = "quarkus.application.version", defaultValue = "") String quarkusAppVersion) {
        this.uploadUseCase = uploadUseCase;
        this.backendUrl = backendUrl;
        this.quarkusAppVersion = quarkusAppVersion != null ? quarkusAppVersion.trim() : "";
    }

    public record UploadCharacteristicsRequest(String playerId, String name, Integer pp) {}
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
        // Ensure name is provided and not empty
        if (request.name() == null || request.name().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("name is required").build();
        }
        // Ensure pp is provided (nullable Integer protects against default 0 when field absent)
        if (request.pp() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("pp is required").build();
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
        // Load the userscript from resources and replace placeholders
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("krabot-characteristics.user.js")) {
            if (in == null) return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("userscript not found").build();
            String content = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String hostWithProtocol = extractHost(backendUrl);
            // compute host-only for @connect (no http://). e.g. localhost:8080
            String hostOnly = hostWithProtocol.replaceFirst("^https?://", "").replaceAll("/.*$", "");
            String hostNoPort = hostOnly.replaceFirst(":\\d+$", "");
            // determine project version: prefer quarkus.application.version when provided
            String projectVersion = (quarkusAppVersion != null && !quarkusAppVersion.isBlank()) ? quarkusAppVersion : computeProjectVersion();
            content = content.replace("__BACKEND_URL__", backendUrl)
                    .replace("__BACKEND_HOST__", hostOnly)
                    .replace("__BACKEND_HOST_NO_PORT__", hostNoPort)
                    .replace("__PROJECT_VERSION__", projectVersion);
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

    private String computeProjectVersion() {
        // 1) try git.properties on classpath (git-commit-id plugin)
        try (InputStream gis = getClass().getClassLoader().getResourceAsStream("git.properties")) {
            if (gis != null) {
                Properties gp = new Properties();
                gp.load(gis);
                String v = gp.getProperty("git.build.version");
                if (v != null && !v.isBlank()) return v.trim();
            }
        } catch (Exception ignored) {}

        // 2) try META-INF/maven/.../pom.properties
        try {
            String path = String.format("META-INF/maven/%s/%s/pom.properties", "arn.roub.krabot", "krabotnotif");
            try (InputStream pis = getClass().getClassLoader().getResourceAsStream(path)) {
                if (pis != null) {
                    Properties pp = new Properties();
                    pp.load(pis);
                    String v = pp.getProperty("version");
                    if (v != null && !v.isBlank()) return v.trim();
                }
            }
        } catch (Exception ignored) {}

        // 3) try reading Implementation-Version from manifest
        try {
            String v = getClass().getPackage().getImplementationVersion();
            if (v != null && !v.isBlank()) return v.trim();
        } catch (Exception ignored) {}

        // fallback
        return "DEV";
    }
}
