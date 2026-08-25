package co.sena.iam.adapter.out.persistence;

import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.domain.model.RefreshTokenRecord;
import jakarta.persistence.EntityManager;
import java.time.ZoneOffset;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Outbound adapter for {@code session.refresh_token}. */
@Repository
class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final EntityManager entityManager;

    JpaRefreshTokenRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void save(RefreshTokenRecord token) {
        entityManager.persist(new RefreshTokenEntity(
                token.id(),
                token.userId(),
                token.tokenHash(),
                token.deviceHint(),
                token.ipAddress(),
                token.expiresAt().atOffset(ZoneOffset.UTC),
                token.createdAt().atOffset(ZoneOffset.UTC)));
    }
}
