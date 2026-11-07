package arn.roub.krabot.infrastructure.adapter.in.rest;

import arn.roub.krabot.domain.port.in.CheckSleepUseCase;
import arn.roub.krabot.domain.port.in.DelayKramailCheckUseCase;
import arn.roub.krabot.shared.exception.ScrapingException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KramailCheckResourceTest {

    private DelayKramailCheckUseCase delayUseCase;
    private CheckSleepUseCase sleepUseCase;
    private KramailCheckResource resource;

    @BeforeEach
    void setUp() {
        delayUseCase = mock(DelayKramailCheckUseCase.class);
        sleepUseCase = mock(CheckSleepUseCase.class);
        resource = new KramailCheckResource(delayUseCase, sleepUseCase, "https://krabot.example.org:8443");
    }

    @Test
    void delay_returnsNextExecution() {
        Instant next = Instant.parse("2026-01-01T12:00:00Z");
        when(delayUseCase.delay()).thenReturn(next);

        Response response = resource.delay();

        assertEquals(200, response.getStatus());
        assertEquals(new KramailCheckResource.DelayResponseDto(next), response.getEntity());
    }

    @Test
    void sleepCheck_success() {
        Response response = resource.triggerSleepCheck();

        verify(sleepUseCase).execute();
        assertEquals(200, response.getStatus());
        assertTrue(((KramailCheckResource.SleepCheckResponseDto) response.getEntity()).success());
    }

    @Test
    void sleepCheck_failureReturns500() {
        doThrow(new ScrapingException("kraland down")).when(sleepUseCase).execute();

        Response response = resource.triggerSleepCheck();

        assertEquals(500, response.getStatus());
        KramailCheckResource.SleepCheckResponseDto dto = (KramailCheckResource.SleepCheckResponseDto) response.getEntity();
        assertFalse(dto.success());
        assertTrue(dto.message().contains("kraland down"));
    }

    @Test
    void userscript_containsBackendUrlAndHost() {
        String script = (String) resource.getUserscript().getEntity();

        assertTrue(script.contains("const BACKEND_URL = 'https://krabot.example.org:8443';"));
        assertTrue(script.contains("// @connect      krabot.example.org:8443"));
    }
}
