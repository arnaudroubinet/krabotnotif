package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.model.Characteristic;
import arn.roub.krabot.domain.model.UserSummary;
import arn.roub.krabot.domain.port.out.CharacteristicsPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadCharacteristicsUseCaseImplTest {

    @Mock
    private CharacteristicsPort port;
    @InjectMocks
    private UploadCharacteristicsUseCaseImpl useCase;

    @Test
    void upload_savesCharacteristicInNamespace() {
        useCase.upload("ns", "42", "Bob", 7);

        ArgumentCaptor<Characteristic> captor = ArgumentCaptor.forClass(Characteristic.class);
        verify(port).save(eq("ns"), captor.capture());
        assertEquals("42", captor.getValue().playerId());
        assertEquals("Bob", captor.getValue().name());
        assertEquals(7, captor.getValue().pp());
        assertNotNull(captor.getValue().updatedAt());
    }

    @Test
    void upload_nullNameBecomesEmpty() {
        useCase.upload("ns", "42", null, 0);

        ArgumentCaptor<Characteristic> captor = ArgumentCaptor.forClass(Characteristic.class);
        verify(port).save(eq("ns"), captor.capture());
        assertEquals("", captor.getValue().name());
    }

    @Test
    void upload_rejectsBlankPlayerId() {
        assertThrows(IllegalArgumentException.class, () -> useCase.upload("ns", " ", "Bob", 1));
        assertThrows(IllegalArgumentException.class, () -> useCase.upload("ns", null, "Bob", 1));
        verify(port, never()).save(anyString(), any());
    }

    @Test
    void upload_rejectsNegativePp() {
        assertThrows(IllegalArgumentException.class, () -> useCase.upload("ns", "42", "Bob", -1));
        verify(port, never()).save(anyString(), any());
    }

    @Test
    void getUsers_delegatesToPort() {
        List<UserSummary> users = List.of(new UserSummary("1", "A", 3, Instant.EPOCH));
        when(port.findAllUsers("ns")).thenReturn(users);

        assertEquals(users, useCase.getUsers("ns"));
    }

    @Test
    void getUserPP_mapsCharacteristic() {
        when(port.findByPlayerId("ns", "1")).thenReturn(Optional.of(new Characteristic("1", "A", 12, Instant.EPOCH)));
        when(port.findByPlayerId("ns", "2")).thenReturn(Optional.empty());

        assertEquals(Optional.of(12), useCase.getUserPP("ns", "1"));
        assertEquals(Optional.empty(), useCase.getUserPP("ns", "2"));
    }

    @Test
    void getUser_delegatesToPort() {
        Characteristic c = new Characteristic("1", "A", 12, Instant.EPOCH);
        when(port.findByPlayerId("ns", "1")).thenReturn(Optional.of(c));

        assertEquals(Optional.of(c), useCase.getUser("ns", "1"));
    }
}
