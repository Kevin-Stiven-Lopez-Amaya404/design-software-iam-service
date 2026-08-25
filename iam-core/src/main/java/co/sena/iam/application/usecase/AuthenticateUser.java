package co.sena.iam.application.usecase;

import co.sena.iam.application.port.in.AuthenticateUserUseCase;
import co.sena.iam.application.port.in.AuthenticationResult;
import co.sena.iam.application.port.in.LoginCommand;
import co.sena.iam.application.port.out.AccessTokenIssuer;
import co.sena.iam.application.port.out.LoginAuditRepository;
import co.sena.iam.application.port.out.PasswordHasher;
import co.sena.iam.application.port.out.RefreshTokenGenerator;
import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.application.port.out.UserAccessRepository;
import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.domain.exception.AccountLockedException;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import co.sena.iam.domain.model.LockoutPolicy;
import co.sena.iam.domain.model.LockoutState;
import co.sena.iam.domain.model.LoginAttempt;
import co.sena.iam.domain.model.LoginOutcome;
import co.sena.iam.domain.model.RefreshTokenRecord;
import co.sena.iam.domain.model.RefreshTokenSecret;
import co.sena.iam.domain.model.TokenPolicy;
import co.sena.iam.domain.model.User;
import co.sena.iam.domain.model.UserAccess;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * HU-IAM-001 E1/E2/E3.
 *
 * <p>Every attempt lands in {@code identity_audit.audit_login}, including the ones that end
 * in an exception, so the ports must not share one rollback scope with the caller.
 */
public final class AuthenticateUser implements AuthenticateUserUseCase {

    private final UserRepository users;
    private final UserAccessRepository userAccess;
    private final LoginAuditRepository auditLog;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokens;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final Clock clock;

    public AuthenticateUser(
            UserRepository users,
            UserAccessRepository userAccess,
            LoginAuditRepository auditLog,
            RefreshTokenRepository refreshTokens,
            PasswordHasher passwordHasher,
            AccessTokenIssuer accessTokens,
            RefreshTokenGenerator refreshTokenGenerator,
            Clock clock) {
        this.users = Objects.requireNonNull(users, "users");
        this.userAccess = Objects.requireNonNull(userAccess, "userAccess");
        this.auditLog = Objects.requireNonNull(auditLog, "auditLog");
        this.refreshTokens = Objects.requireNonNull(refreshTokens, "refreshTokens");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
        this.accessTokens = Objects.requireNonNull(accessTokens, "accessTokens");
        this.refreshTokenGenerator = Objects.requireNonNull(refreshTokenGenerator, "refreshTokenGenerator");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public AuthenticationResult authenticate(LoginCommand command) {
        Objects.requireNonNull(command, "command");
        Instant now = clock.instant();
        String email = command.email().trim();

        Optional<User> found = users.findByEmail(email);
        if (found.isEmpty()) {
            audit(null, email, LoginOutcome.USER_NOT_FOUND, command, now);
            throw new InvalidCredentialsException();
        }
        User user = found.get();

        // E3 — an account still serving its lockout never gets to the password check.
        if (user.isLockedAt(now)) {
            audit(user.id(), email, LoginOutcome.ACCOUNT_LOCKED, command, now);
            throw new AccountLockedException(user.lockout().lockedUntil());
        }

        if (!passwordHasher.matches(command.password(), user.passwordHash())) {
            // E2 — count the failure (which may arm the lock) before refusing.
            LockoutState next = LockoutPolicy.afterFailedAttempt(user.lockout(), now);
            users.applyLockoutState(user.id(), next);
            audit(user.id(), email, LoginOutcome.INVALID_PASSWORD, command, now);
            // The attempt that trips the threshold is still answered as a credential failure;
            // ACCOUNT_LOCKED starts on the next attempt.
            throw new InvalidCredentialsException();
        }

        // A disabled account is refused like a wrong password so the answer leaks nothing.
        // audit_login has no DISABLED outcome today, hence INVALID_PASSWORD.
        if (!user.active()) {
            audit(user.id(), email, LoginOutcome.INVALID_PASSWORD, command, now);
            throw new InvalidCredentialsException();
        }

        return issueSession(user, command, now);
    }

    /** E1 — clear the counter, mint both tokens, persist the session, audit the success. */
    private AuthenticationResult issueSession(User user, LoginCommand command, Instant now) {
        users.registerSuccessfulLogin(user.id(), now);

        UserAccess access = userAccess.findAccessFor(user.id());
        Instant accessExpiresAt = now.plus(TokenPolicy.ACCESS_TOKEN_TTL);
        String accessToken = accessTokens.issue(user, access, now, accessExpiresAt);

        RefreshTokenSecret refresh = refreshTokenGenerator.generate();
        refreshTokens.save(new RefreshTokenRecord(
                UUID.randomUUID(),
                user.id(),
                refresh.hash(),
                command.deviceHint(),
                command.ipAddress(),
                now.plus(TokenPolicy.REFRESH_TOKEN_TTL),
                now));

        audit(user.id(), user.email(), LoginOutcome.SUCCESS, command, now);

        return new AuthenticationResult(
                accessToken,
                refresh.value(),
                TokenPolicy.ACCESS_TOKEN_TTL.toSeconds(),
                user.withLockout(LockoutPolicy.afterSuccessfulLogin()),
                access);
    }

    private void audit(UUID userId, String email, LoginOutcome outcome, LoginCommand command, Instant now) {
        auditLog.record(new LoginAttempt(
                userId, email, outcome, command.ipAddress(), command.userAgent(), now));
    }
}
