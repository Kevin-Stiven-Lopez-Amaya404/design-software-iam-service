package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.User;
import co.sena.iam.domain.model.UserAccess;
import java.time.Instant;
import java.util.UUID;

/** Mint a signed access token. */
public interface AccessTokenIssuer {

    String issue(User user, UserAccess access, UUID sessionId, Instant issuedAt, Instant expiresAt);
}
