package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.User;
import co.sena.iam.domain.model.UserAccess;
import java.time.Instant;

/** Outbound port that mints the signed access token (JWT in the adapter). */
public interface AccessTokenIssuer {

    String issue(User user, UserAccess access, Instant issuedAt, Instant expiresAt);
}
