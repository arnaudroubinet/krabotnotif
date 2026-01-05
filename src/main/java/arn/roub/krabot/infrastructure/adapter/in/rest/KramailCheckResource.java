package arn.roub.krabot.infrastructure.adapter.in.rest;

import arn.roub.krabot.domain.port.in.DelayKramailCheckUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;

/**
 * Adapter REST pour contrôler la vérification des kramails.
 */
@Path("krabot/kramail-check")
@ApplicationScoped
public class KramailCheckResource {

    private final DelayKramailCheckUseCase delayKramailCheckUseCase;
    private final String backendUrl;
    private final String scriptVersion;

    public KramailCheckResource(
            DelayKramailCheckUseCase delayKramailCheckUseCase,
            @ConfigProperty(name = "krabot.backend.url") String backendUrl
    ) {
        this.delayKramailCheckUseCase = delayKramailCheckUseCase;
        this.backendUrl = backendUrl;
        this.scriptVersion = String.valueOf(Instant.now().getEpochSecond());
    }

    @POST
    @Path("delay")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delay() {
        Instant nextExecution = delayKramailCheckUseCase.delay();
        return Response.ok(new DelayResponseDto(nextExecution)).build();
    }

    public record DelayResponseDto(Instant nextExecution) {}

    @GET
    @Path("userscript.user.js")
    @Produces("application/javascript")
    public Response getUserscript() {
        String script = """
                // ==UserScript==
                // @name         Krabot Delay Timer
                // @namespace    krabot
                // @version      %s
                // @description  Repousse le timer de vérification Krabot quand Kraland est visité
                // @author       Krabot
                // @match        *://www.kraland.org/*
                // @match        *://kraland.org/*
                // @grant        GM_xmlhttpRequest
                // @connect      %s
                // ==/UserScript==

                (function() {
                    'use strict';

                    const BACKEND_URL = '%s';

                    GM_xmlhttpRequest({
                        method: 'POST',
                        url: BACKEND_URL + '/krabot/kramail-check/delay',
                        onload: function(response) {
                            const data = JSON.parse(response.responseText);
                            const nextExecution = new Date(data.nextExecution).toLocaleString('fr-FR');
                            console.log('[Krabot] Timer delayed successfully. Next execution: ' + nextExecution);
                        },
                        onerror: function(error) {
                            console.error('[Krabot] Failed to delay timer:', error);
                        }
                    });
                })();
                """.formatted(scriptVersion, extractHost(backendUrl), backendUrl);

        return Response.ok(script).build();
    }

    private String extractHost(String url) {
        return url.replaceAll("^https?://", "").replaceAll("/.*$", "");
    }
}
