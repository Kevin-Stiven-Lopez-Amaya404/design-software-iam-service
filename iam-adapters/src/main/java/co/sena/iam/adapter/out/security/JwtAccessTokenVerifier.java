package co.sena.iam.adapter.out.security;

import co.sena.iam.application.port.out.AccessTokenVerifier;
import co.sena.iam.domain.exception.UnauthenticatedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Local verification of the tokens this service issued (signature, issuer and expiry). */
@Component
class JwtAccessTokenVerifier implements AccessTokenVerifier {

    private final RsaSigningKeys keys;

    JwtAccessTokenVerifier(RsaSigningKeys keys) {
        this.keys = keys;
    }

    @Override
    public UUID verifyAndExtractUserId(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new UnauthenticatedException("Missing access token");
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(keys.publicKey())
                    .requireIssuer(keys.issuer())
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthenticatedException("Access token rejected", e);
        }
    }
}
