package arn.roub.krabot.infrastructure.adapter.out.persistence;

import arn.roub.krabot.domain.model.Characteristic;
import arn.roub.krabot.domain.model.UserSummary;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CharacteristicsMemoryRepositoryTest {

    private final CharacteristicsMemoryRepository repository = new CharacteristicsMemoryRepository();

    @Test
    void saveAndFind_isolatedByNamespace() {
        repository.save("ns1", new Characteristic("1", "Bob", 5, Instant.EPOCH));

        Characteristic found = repository.findByPlayerId("ns1", "1").orElseThrow();
        assertEquals("Bob", found.name());
        assertEquals(5, found.pp());
        assertTrue(found.updatedAt().isAfter(Instant.EPOCH), "timestamp refreshed on save");
        assertTrue(repository.findByPlayerId("ns2", "1").isEmpty());
        assertTrue(repository.findAllUsers("ns2").isEmpty());
    }

    @Test
    void save_overwritesExistingPlayer() {
        repository.save("ns", new Characteristic("1", "Bob", 5));
        repository.save("ns", new Characteristic("1", "Bob", 9));

        List<UserSummary> users = repository.findAllUsers("ns");
        assertEquals(1, users.size());
        assertEquals(9, users.getFirst().pp());
    }

    @Test
    void nullNamespace_usesDefault() {
        repository.save(null, new Characteristic("1", "Bob", 5));

        assertTrue(repository.findByPlayerId(null, "1").isPresent());
        assertEquals(1, repository.findAllUsers(null).size());
    }
}
