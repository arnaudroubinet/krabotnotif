package arn.roub.krabot.domain.port.out;

import arn.roub.krabot.domain.model.Characteristic;
import arn.roub.krabot.domain.model.UserSummary;

import java.util.List;
import java.util.Optional;

public interface CharacteristicsPort {
    void save(String namespaceApiKey, Characteristic characteristic);
    Optional<Characteristic> findByPlayerId(String namespaceApiKey, String playerId);
    List<UserSummary> findAllUsers(String namespaceApiKey);
}
