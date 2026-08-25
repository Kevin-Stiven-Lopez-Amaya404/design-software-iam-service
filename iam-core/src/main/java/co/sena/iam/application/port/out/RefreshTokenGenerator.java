package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.RefreshTokenSecret;

/** Outbound port that mints an opaque refresh token together with the digest to persist. */
public interface RefreshTokenGenerator {

    RefreshTokenSecret generate();
}
