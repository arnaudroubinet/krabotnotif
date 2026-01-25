package arn.roub.krabot.infrastructure.adapter.out.persistence;

import arn.roub.krabot.domain.model.Characteristic;
import arn.roub.krabot.domain.model.UserSummary;
import arn.roub.krabot.domain.port.out.CharacteristicsPort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CharacteristicsMemoryRepository implements CharacteristicsPort {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Characteristic>> store = new ConcurrentHashMap<>();

    @Override
    public void save(String namespaceApiKey, Characteristic characteristic) {
        store.computeIfAbsent(namespaceApiKey == null ? "default" : namespaceApiKey, k -> new ConcurrentHashMap<>())
                .put(characteristic.playerId(), new Characteristic(characteristic.playerId(), characteristic.name(), characteristic.pp(), Instant.now()));
    }

    @Override
    public Optional<Characteristic> findByPlayerId(String namespaceApiKey, String playerId) {
        Map<String, Characteristic> map = store.get(namespaceApiKey == null ? "default" : namespaceApiKey);
        if (map == null) return Optional.empty();
        return Optional.ofNullable(map.get(playerId));
    }

    @Override
    public List<UserSummary> findAllUsers(String namespaceApiKey) {
        Map<String, Characteristic> map = store.get(namespaceApiKey == null ? "default" : namespaceApiKey);
        List<UserSummary> result = new ArrayList<>();
        if (map == null) return result;
        for (Characteristic c : map.values()) {
            result.add(new UserSummary(c.playerId(), c.name(), c.pp()));
        }
        return result;
    }
}
