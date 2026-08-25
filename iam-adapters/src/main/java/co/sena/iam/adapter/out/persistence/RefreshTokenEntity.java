package co.sena.iam.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Row of {@code session.refresh_token}. Stores the digest, never the token itself. */
@Entity
@Table(name = "refresh_token", schema = "session")
class RefreshTokenEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "device_hint", length = 200)
    private String deviceHint;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected RefreshTokenEntity() {
    }

    RefreshTokenEntity(
            UUID id,
            UUID userId,
            String tokenHash,
            String deviceHint,
            String ipAddress,
            OffsetDateTime expiresAt,
            OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.deviceHint = deviceHint;
        this.ipAddress = ipAddress;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.revoked = false;
    }
}
