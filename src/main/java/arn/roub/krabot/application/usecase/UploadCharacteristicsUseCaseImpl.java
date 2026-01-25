package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.model.Characteristic;
import arn.roub.krabot.domain.model.UserSummary;
import arn.roub.krabot.domain.port.in.UploadCaracteristiquesUseCase;
import arn.roub.krabot.domain.port.out.CharacteristicsPort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class UploadCharacteristicsUseCaseImpl implements UploadCaracteristiquesUseCase {

    private final CharacteristicsPort repository;

    public UploadCharacteristicsUseCaseImpl(CharacteristicsPort repository) {
        this.repository = repository;
    }

    @Override
    public void upload(String namespaceApiKey, String playerId, String name, int pp) {
        if (playerId == null || playerId.trim().isEmpty()) throw new IllegalArgumentException("playerId required");
        if (pp < 0) throw new IllegalArgumentException("pp must be >= 0");
        var c = new Characteristic(playerId, name == null ? "" : name, pp, Instant.now());
        repository.save(namespaceApiKey, c);
    }

    @Override
    public List<UserSummary> getUsers(String namespaceApiKey) {
        return repository.findAllUsers(namespaceApiKey);
    }

    @Override
    public Optional<Integer> getUserPP(String namespaceApiKey, String playerId) {
        return repository.findByPlayerId(namespaceApiKey, playerId).map(Characteristic::pp);
    }
}
