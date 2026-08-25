package co.sena.iam.testsupport;

import co.sena.iam.application.port.out.AccessTokenIssuer;
import co.sena.iam.application.port.out.PasswordHasher;
import co.sena.iam.application.port.out.RefreshTokenGenerator;
import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.application.port.out.UserAccessRepository;
import co.sena.iam.domain.model.RefreshTokenRecord;
import co.sena.iam.domain.model.RefreshTokenSecret;
import co.sena.iam.domain.model.UserAccess;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Small hand-written doubles for the outbound ports the use cases talk to. */
public final class TestDoubles {

    private TestDoubles() {
    }

    /** "Hashing" is a prefix so tests can tell a right password from a wrong one. */
    public static final String HASH_PREFIX = "hashed:";

    public static PasswordHasher passwordHasher() {
        return (raw, hash) -> (HASH_PREFIX + raw).equals(hash);
    }

    public static String hashOf(String rawPassword) {
        return HASH_PREFIX + rawPassword;
    }

    public static AccessTokenIssuer accessTokenIssuer(String token) {
        return (user, access, issuedAt, expiresAt) -> token;
    }

    public static RefreshTokenGenerator refreshTokenGenerator(String value) {
        return () -> new RefreshTokenSecret(value, "digest-of-" + value);
    }

    public static UserAccessRepository userAccess(UserAccess access) {
        return userId -> access;
    }

    /** Collects saved refresh tokens. */
    public static final class RecordingRefreshTokenRepository implements RefreshTokenRepository {

        private final List<RefreshTokenRecord> saved = new ArrayList<>();

        @Override
        public void save(RefreshTokenRecord token) {
            saved.add(token);
        }

        public List<RefreshTokenRecord> saved() {
            return List.copyOf(saved);
        }
    }

    /** Fails the test if a refresh token is persisted on a path that must not create a session. */
    public static RefreshTokenRepository refuseToSave() {
        return token -> {
            throw new AssertionError("no session must be created for user " + token.userId());
        };
    }

    public static UUID uuid(String last) {
        return UUID.fromString("00000000-0000-0000-0000-0000000000" + last);
    }
}
