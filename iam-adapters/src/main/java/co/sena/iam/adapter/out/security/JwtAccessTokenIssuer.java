package co.sena.iam.adapter.out.security;

import co.sena.iam.application.port.out.AccessTokenIssuer;
import co.sena.iam.domain.model.User;
import co.sena.iam.domain.model.UserAccess;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * RS256 access token, as declared by the contract ({@code bearerAuth}: JWT RS256 issued by
 * iam-service and verified locally through JWKS).
 */
@Component
class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private final RsaSigningKeys keys;

    JwtAccessTokenIssuer(RsaSigningKeys keys) {
        this.keys = keys;
    }

    @Override
    public String issue(User user, UserAccess access, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .header().keyId(keys.keyId()).and()
                .id(UUID.randomUUID().toString())
                .issuer(keys.issuer())
                .subject(user.id().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("email", user.email())
                .claim("name", user.fullName())
                .claim("actor_type", user.actorType().name())
                .claim("actor_id", user.actorId() == null ? null : user.actorId().toString())
                .claim("roles", access.roleNames())
                .claim("features", access.featureCodes())
                .claim("training_center_id", trainingCenterId(access))
                .signWith(keys.privateKey(), Jwts.SIG.RS256)
                .compact();
    }

    private static String trainingCenterId(UserAccess access) {
        UUID id = access.primaryTrainingCenterId();
        return id == null ? null : id.toString();
    }
}
