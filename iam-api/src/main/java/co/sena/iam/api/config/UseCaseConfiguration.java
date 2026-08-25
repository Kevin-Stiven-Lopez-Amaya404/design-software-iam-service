package co.sena.iam.api.config;

import co.sena.iam.application.port.in.AuthenticateUserUseCase;
import co.sena.iam.application.port.in.GetCurrentUserUseCase;
import co.sena.iam.application.port.out.AccessTokenIssuer;
import co.sena.iam.application.port.out.AccessTokenVerifier;
import co.sena.iam.application.port.out.LoginAuditRepository;
import co.sena.iam.application.port.out.PasswordHasher;
import co.sena.iam.application.port.out.RefreshTokenGenerator;
import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.application.port.out.UserAccessRepository;
import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.application.usecase.AuthenticateUser;
import co.sena.iam.application.usecase.GetCurrentUser;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring only. The use cases live in {@code iam-core} and know nothing about Spring, so the
 * deployable is where their ports get bound to the adapters.
 */
@Configuration(proxyBeanMethods = false)
class UseCaseConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    AuthenticateUserUseCase authenticateUserUseCase(
            UserRepository users,
            UserAccessRepository userAccess,
            LoginAuditRepository auditLog,
            RefreshTokenRepository refreshTokens,
            PasswordHasher passwordHasher,
            AccessTokenIssuer accessTokens,
            RefreshTokenGenerator refreshTokenGenerator,
            Clock clock) {
        return new AuthenticateUser(
                users, userAccess, auditLog, refreshTokens,
                passwordHasher, accessTokens, refreshTokenGenerator, clock);
    }

    @Bean
    GetCurrentUserUseCase getCurrentUserUseCase(
            AccessTokenVerifier accessTokenVerifier,
            UserRepository users,
            UserAccessRepository userAccess) {
        return new GetCurrentUser(accessTokenVerifier, users, userAccess);
    }
}
