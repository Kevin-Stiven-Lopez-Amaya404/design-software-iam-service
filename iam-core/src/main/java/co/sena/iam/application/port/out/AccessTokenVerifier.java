package co.sena.iam.application.port.out;

import java.util.UUID;

/** Outbound port that validates an access token and returns its subject. */
public interface AccessTokenVerifier {

    /**
     * @throws co.sena.iam.domain.exception.UnauthenticatedException signature, shape or
     *                                                              expiry rejected the token
     */
    UUID verifyAndExtractUserId(String accessToken);
}
