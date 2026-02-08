package arn.roub.krabot.infrastructure.adapter.in.rest;

import arn.roub.krabot.domain.port.in.CheckSleepUseCase;
import arn.roub.krabot.domain.port.in.DelayKramailCheckUseCase;
import arn.roub.krabot.domain.port.in.DelaySleepCheckUseCase;
import arn.roub.krabot.domain.port.in.ResetGeneralNotificationUseCase;
import arn.roub.krabot.domain.port.in.ResetKramailsNotificationUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Adapter REST pour contrôler la vérification des kramails.
 */
@Path("krabot/kramail-check")
@ApplicationScoped
public class KramailCheckResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(KramailCheckResource.class);

    private final DelayKramailCheckUseCase delayKramailCheckUseCase;
    private final DelaySleepCheckUseCase delaySleepCheckUseCase;
    private final CheckSleepUseCase checkSleepUseCase;
    private final ResetGeneralNotificationUseCase resetGeneralNotificationUseCase;
    private final ResetKramailsNotificationUseCase resetKramailsNotificationUseCase;
    private final String backendUrl;
    private final String scriptVersion;

    public KramailCheckResource(
            DelayKramailCheckUseCase delayKramailCheckUseCase,
            DelaySleepCheckUseCase delaySleepCheckUseCase,
            CheckSleepUseCase checkSleepUseCase,
            ResetGeneralNotificationUseCase resetGeneralNotificationUseCase,
            ResetKramailsNotificationUseCase resetKramailsNotificationUseCase,
            @ConfigProperty(name = "krabot.backend.url") String backendUrl
    ) {
        this.delayKramailCheckUseCase = delayKramailCheckUseCase;
        this.delaySleepCheckUseCase = delaySleepCheckUseCase;
        this.checkSleepUseCase = checkSleepUseCase;
        this.resetGeneralNotificationUseCase = resetGeneralNotificationUseCase;
        this.resetKramailsNotificationUseCase = resetKramailsNotificationUseCase;
        this.backendUrl = backendUrl;
        this.scriptVersion = String.valueOf(Instant.now().getEpochSecond());
    }

    @POST
    @Path("delay")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delay() {
        resetGeneralNotificationUseCase.execute();
        resetKramailsNotificationUseCase.execute();
        Instant nextKramailExecution = delayKramailCheckUseCase.delay();
        Instant nextSleepExecution = delaySleepCheckUseCase.delay();
        return Response.ok(new DelayResponseDto(nextKramailExecution, nextSleepExecution)).build();
    }

    public record DelayResponseDto(Instant nextKramailExecution, Instant nextSleepExecution) {}

    @POST
    @Path("sleep-check")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerSleepCheck() {
        LOGGER.info("Manual sleep check triggered via REST API");
        try {
            checkSleepUseCase.execute();
            return Response.ok(new SleepCheckResponseDto(true, "Sleep check executed successfully")).build();
        } catch (Exception e) {
            LOGGER.error("Manual sleep check failed: {}", e.getMessage());
            return Response.serverError()
                    .entity(new SleepCheckResponseDto(false, "Sleep check failed: " + e.getMessage()))
                    .build();
        }
    }

    public record SleepCheckResponseDto(boolean success, String message) {}

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
                            const nextKramailExecution = new Date(data.nextKramailExecution).toLocaleString('fr-FR');
                            const nextSleepExecution = new Date(data.nextSleepExecution).toLocaleString('fr-FR');
                            console.log('[Krabot] Timers delayed successfully. Next kramail check: ' + nextKramailExecution + ', Next sleep check: ' + nextSleepExecution);
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
