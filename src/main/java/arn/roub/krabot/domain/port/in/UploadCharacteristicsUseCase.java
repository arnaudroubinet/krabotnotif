package arn.roub.krabot.domain.port.in;

import arn.roub.krabot.domain.model.UserSummary;

import java.util.List;
import java.util.Optional;

/**
 * Use case port for uploading and querying characteristics.
 */
public interface UploadCharacteristicsUseCase {

    void upload(String namespaceApiKey, String playerId, String name, int pp);

    List<UserSummary> getUsers(String namespaceApiKey);

    Optional<Integer> getUserPP(String namespaceApiKey, String playerId);
}
