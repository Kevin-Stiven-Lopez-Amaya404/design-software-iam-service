package co.sena.iam.api.config;

import co.sena.iam.application.port.in.AuthenticateUserUseCase;
import co.sena.iam.application.port.in.GetCurrentUserUseCase;
import co.sena.iam.application.port.in.ListModulesUseCase;
import co.sena.iam.application.port.in.ListRoleFeaturesUseCase;
import co.sena.iam.application.port.in.ListRolesUseCase;
import co.sena.iam.application.port.in.ListUserSessionsUseCase;
import co.sena.iam.application.port.in.LogoutUseCase;
import co.sena.iam.application.port.in.RefreshAccessTokenUseCase;
import co.sena.iam.application.port.in.RevokeUserSessionUseCase;
import co.sena.iam.application.port.out.AccessTokenIssuer;
import co.sena.iam.application.port.out.AccessTokenVerifier;
import co.sena.iam.application.port.out.LoginAuditRepository;
import co.sena.iam.application.port.out.PasswordHasher;
import co.sena.iam.application.port.out.RbacCatalogRepository;
import co.sena.iam.application.port.out.RefreshTokenGenerator;
import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.application.port.out.UserAccessRepository;
import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.application.usecase.AuthenticateUser;
import co.sena.iam.application.usecase.GetCurrentUser;
import co.sena.iam.application.usecase.ListModules;
import co.sena.iam.application.usecase.ListRoleFeatures;
import co.sena.iam.application.usecase.ListRoles;
import co.sena.iam.application.usecase.ListUserSessions;
import co.sena.iam.application.usecase.Logout;
import co.sena.iam.application.usecase.RefreshAccessToken;
import co.sena.iam.application.usecase.RevokeUserSession;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wiring only — use cases live in iam-core and know nothing about Spring. */
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

    @Bean
    RefreshAccessTokenUseCase refreshAccessTokenUseCase(
            UserRepository users,
            UserAccessRepository userAccess,
            RefreshTokenRepository refreshTokens,
            AccessTokenIssuer accessTokens,
            RefreshTokenGenerator refreshTokenGenerator,
            Clock clock) {
        return new RefreshAccessToken(users, userAccess, refreshTokens, accessTokens, refreshTokenGenerator, clock);
    }

    @Bean
    LogoutUseCase logoutUseCase(RefreshTokenRepository refreshTokens, Clock clock) {
        return new Logout(refreshTokens, clock);
    }

    @Bean
    ListUserSessionsUseCase listUserSessionsUseCase(RefreshTokenRepository refreshTokens) {
        return new ListUserSessions(refreshTokens);
    }

    @Bean
    RevokeUserSessionUseCase revokeUserSessionUseCase(RefreshTokenRepository refreshTokens, Clock clock) {
        return new RevokeUserSession(refreshTokens, clock);
    }

    @Bean
    ListModulesUseCase listModulesUseCase(RbacCatalogRepository catalog) {
        return new ListModules(catalog);
    }

    @Bean
    ListRolesUseCase listRolesUseCase(RbacCatalogRepository catalog) {
        return new ListRoles(catalog);
    }

    @Bean
    ListRoleFeaturesUseCase listRoleFeaturesUseCase(RbacCatalogRepository catalog) {
        return new ListRoleFeatures(catalog);
    }
}
