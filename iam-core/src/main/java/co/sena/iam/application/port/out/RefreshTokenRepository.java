package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.RefreshTokenRecord;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Outbound port over {@code session.refresh_token}. */
public interface RefreshTokenRepository {

    void save(RefreshTokenRecord token);

    Optional<RefreshTokenRecord> findActiveByHash(String tokenHash);

    Optional<RefreshTokenRecord> findById(UUID sessionId);

    void revoke(UUID sessionId, Instant revokedAt);

    List<RefreshTokenRecord> findByUserId(UUID userId);
}
