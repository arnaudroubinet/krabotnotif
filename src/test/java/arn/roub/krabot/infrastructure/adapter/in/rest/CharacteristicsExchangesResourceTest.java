package arn.roub.krabot.infrastructure.adapter.in.rest;

import arn.roub.krabot.domain.model.UserSummary;
import arn.roub.krabot.domain.port.in.UploadCharacteristicsUseCase;
import arn.roub.krabot.infrastructure.adapter.in.rest.CharacteristicsExchangesResource.UploadCharacteristicsRequest;
import arn.roub.krabot.infrastructure.adapter.in.rest.CharacteristicsExchangesResource.UserSummaryResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CharacteristicsExchangesResourceTest {

    private UploadCharacteristicsUseCase useCase;
    private CharacteristicsExchangesResource resource;

    @BeforeEach
    void setUp() {
        useCase = mock(UploadCharacteristicsUseCase.class);
        resource = new CharacteristicsExchangesResource(useCase, "http://localhost:8080", "1.2.3");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void upload_requiresApiKey(String apiKey) {
        Response response = resource.uploadCharacteristics(apiKey, new UploadCharacteristicsRequest("1", "Bob", 3));

        assertEquals(401, response.getStatus());
        verifyNoInteractions(useCase);
    }

    @Test
    void upload_validatesBody() {
        assertEquals(400, resource.uploadCharacteristics("k", null).getStatus());
        assertEquals(400, resource.uploadCharacteristics("k", new UploadCharacteristicsRequest(" ", "Bob", 3)).getStatus());
        assertEquals(400, resource.uploadCharacteristics("k", new UploadCharacteristicsRequest("1", "", 3)).getStatus());
        assertEquals(400, resource.uploadCharacteristics("k", new UploadCharacteristicsRequest("1", "Bob", null)).getStatus());
        assertEquals(400, resource.uploadCharacteristics("k", new UploadCharacteristicsRequest("1", "Bob", -1)).getStatus());
        verify(useCase, never()).upload(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void upload_delegatesWithTrimmedNamespace() {
        Response response = resource.uploadCharacteristics(" key ", new UploadCharacteristicsRequest("1", "Bob", 3));

        assertEquals(200, response.getStatus());
        verify(useCase).upload("key", "1", "Bob", 3);
    }

    @Test
    void getUsers_requiresApiKey() {
        assertEquals(401, resource.getUsers(null).getStatus());
    }

    @Test
    void getUsers_mapsSummaries() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(useCase.getUsers("key")).thenReturn(List.of(new UserSummary("1", "Bob", 3, now)));

        Response response = resource.getUsers("key");

        assertEquals(200, response.getStatus());
        assertEquals(List.of(new UserSummaryResponse("1", "Bob", 3, now)), response.getEntity());
    }

    @Test
    void userScript_replacesPlaceholders() {
        Response response = resource.getUserScript();

        assertEquals(200, response.getStatus());
        String script = (String) response.getEntity();
        assertFalse(script.contains("__BACKEND_URL__"));
        assertFalse(script.contains("__BACKEND_HOST__"));
        assertFalse(script.contains("__PROJECT_VERSION__"));
        assertTrue(script.contains("http://localhost:8080"));
        assertTrue(script.contains("1.2.3"));
    }
}
