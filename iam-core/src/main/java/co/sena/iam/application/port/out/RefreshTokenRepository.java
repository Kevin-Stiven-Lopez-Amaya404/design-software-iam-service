package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.RefreshTokenRecord;

/** Outbound port over {@code session.refresh_token}. */
public interface RefreshTokenRepository {

    void save(RefreshTokenRecord token);
}
