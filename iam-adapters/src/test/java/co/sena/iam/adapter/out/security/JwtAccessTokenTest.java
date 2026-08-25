package co.sena.iam.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.sena.iam.domain.exception.UnauthenticatedException;
import co.sena.iam.domain.model.ActorType;
import co.sena.iam.domain.model.LockoutState;
import co.sena.iam.domain.model.RoleAssignment;
import co.sena.iam.domain.model.TokenClaims;
import co.sena.iam.domain.model.TokenPolicy;
import co.sena.iam.domain.model.User;
import co.sena.iam.domain.model.UserAccess;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The signed access token and its local verification. */
class JwtAccessTokenTest {

    private static final String ISSUER = "https://iam.test.sena.edu.co";
    private static final UUID USER_ID = UUID.fromString("c0000000-0000-0000-0000-000000000042");
    private static final UUID SESSION_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001");

    private static String privateKeyPem;
    private static String otherPrivateKeyPem;

    @BeforeAll
    static void generateKeys() throws NoSuchAlgorithmException {
        privateKeyPem = pkcs8Base64(newKeyPair());
        otherPrivateKeyPem = pkcs8Base64(newKeyPair());
    }

    private static KeyPair newKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String pkcs8Base64(KeyPair pair) {
        return Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
    }

    private static RsaSigningKeys keys(String privateKey) {
        return new RsaSigningKeys(new JwtProperties(privateKey, ISSUER, "test-key"));
    }

    private static User user() {
        return new User(USER_ID, "ana@sena.edu.co", "irrelevant", "Ana", "Ríos",
                ActorType.INSTRUCTOR, null, true, LockoutState.clear(), false, null);
    }

    private static UserAccess access() {
        return new UserAccess(
                List.of(new RoleAssignment("INSTRUCTOR", null)),
                List.of("schedule.read"),
                List.of("scheduling"));
    }

    @Test
    @DisplayName("a freshly issued token verifies back to its subject and session id")
    void issuedTokenVerifiesToItsSubjectAndSession() {
        RsaSigningKeys signingKeys = keys(privateKeyPem);
        Instant now = Instant.now();
        String token = new JwtAccessTokenIssuer(signingKeys)
                .issue(user(), access(), SESSION_ID, now, now.plus(TokenPolicy.ACCESS_TOKEN_TTL));

        TokenClaims claims = new JwtAccessTokenVerifier(signingKeys).verify(token);
        assertEquals(USER_ID, claims.userId());
        assertEquals(SESSION_ID, claims.sessionId());
        assertEquals(3, token.split("\\.").length, "a JWS has three parts");
    }

    @Test
    @DisplayName("a token signed by another key is rejected")
    void rejectsTokenSignedByAnotherKey() {
        Instant now = Instant.now();
        String forged = new JwtAccessTokenIssuer(keys(otherPrivateKeyPem))
                .issue(user(), access(), SESSION_ID, now, now.plus(TokenPolicy.ACCESS_TOKEN_TTL));
        JwtAccessTokenVerifier verifier = new JwtAccessTokenVerifier(keys(privateKeyPem));

        assertThrows(UnauthenticatedException.class, () -> verifier.verify(forged));
    }

    @Test
    @DisplayName("an expired token is rejected")
    void rejectsExpiredToken() {
        RsaSigningKeys signingKeys = keys(privateKeyPem);
        Instant longAgo = Instant.now().minusSeconds(3600);
        String expired = new JwtAccessTokenIssuer(signingKeys)
                .issue(user(), access(), SESSION_ID, longAgo, longAgo.plusSeconds(60));

        assertThrows(UnauthenticatedException.class,
                () -> new JwtAccessTokenVerifier(signingKeys).verify(expired));
    }

    @Test
    @DisplayName("a missing or blank token is rejected before any parsing")
    void rejectsMissingToken() {
        JwtAccessTokenVerifier verifier = new JwtAccessTokenVerifier(keys(privateKeyPem));

        assertThrows(UnauthenticatedException.class, () -> verifier.verify(null));
        assertThrows(UnauthenticatedException.class, () -> verifier.verify("  "));
    }

    @Test
    @DisplayName("PEM armour around the key is accepted just like the bare base64")
    void acceptsPemArmouredKey() {
        String pem = "-----BEGIN PRIVATE KEY-----\n" + privateKeyPem + "\n-----END PRIVATE KEY-----";

        assertEquals(keys(privateKeyPem).publicKey(), keys(pem).publicKey());
    }

    @Test
    @DisplayName("startup fails loudly when the signing key is missing or unusable")
    void refusesToStartWithoutAUsableKey() {
        assertThrows(IllegalStateException.class,
                () -> new RsaSigningKeys(new JwtProperties(null, ISSUER, null)));
        assertThrows(IllegalStateException.class,
                () -> new RsaSigningKeys(new JwtProperties("   ", ISSUER, null)));
        assertThrows(IllegalStateException.class,
                () -> new RsaSigningKeys(new JwtProperties(privateKeyPem, null, null)));
        assertThrows(IllegalStateException.class,
                () -> new RsaSigningKeys(new JwtProperties("not-a-key", ISSUER, null)));
    }

    @Test
    @DisplayName("each refresh token is unique and only its digest is meant to be stored")
    void refreshTokensAreUniqueAndHashed() {
        SecureRandomRefreshTokenGenerator generator = new SecureRandomRefreshTokenGenerator();

        var first = generator.generate();
        var second = generator.generate();

        assertNotEquals(first.value(), second.value());
        assertNotEquals(first.value(), first.hash());
        assertEquals(64, first.hash().length(), "SHA-256 as hex");
        assertEquals(first.hash(), SecureRandomRefreshTokenGenerator.sha256Hex(first.value()));
        assertTrue(first.value().length() >= 43, "256 bits of entropy in base64url");
    }
}
