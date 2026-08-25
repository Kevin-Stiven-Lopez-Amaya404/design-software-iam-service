package co.sena.iam.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Persisted side of a session's refresh token. Mirrors {@code session.refresh_token}.
 * Only the digest is stored — never the value handed to the client.
 *
 * @param deviceHint nullable
 * @param ipAddress  nullable
 */
public record RefreshTokenRecord(
        UUID id,
        UUID userId,
        String tokenHash,
        String deviceHint,
        String ipAddress,
        Instant expiresAt,
        Instant createdAt) {

    public RefreshTokenRecord {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(tokenHash, "tokenHash");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}
