package arn.roub.krabot.infrastructure.adapter.in.rest;

import arn.roub.krabot.domain.model.NotificationState;
import arn.roub.krabot.domain.model.ReleaseVersion;
import arn.roub.krabot.domain.port.in.GetCurrentStateUseCase;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StateRestAdapterTest {

    @Test
    void mapsDomainStateToDto() {
        GetCurrentStateUseCase useCase = mock(GetCurrentStateUseCase.class);
        when(useCase.execute()).thenReturn(new NotificationState(
                2, true, ReleaseVersion.of("v1.0.0"), ReleaseVersion.of("v1.1.0")));

        Response response = new StateRestAdapter(useCase).getState();

        assertEquals(200, response.getStatus());
        assertEquals(new StateRestAdapter.StateDto(2, true, "v1.0.0", "v1.1.0"), response.getEntity());
    }
}
