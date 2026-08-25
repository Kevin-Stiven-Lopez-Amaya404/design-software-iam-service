package co.sena.iam.adapter.in.rest;

import co.sena.iam.application.port.in.AuthenticateUserUseCase;
import co.sena.iam.application.port.in.AuthenticatedIdentity;
import co.sena.iam.application.port.in.AuthenticationResult;
import co.sena.iam.application.port.in.GetCurrentUserUseCase;
import co.sena.iam.application.port.in.LoginCommand;
import co.sena.iam.contract.api.AuthApi;
import co.sena.iam.contract.model.LoginRequest;
import co.sena.iam.contract.model.MeResponse;
import co.sena.iam.contract.model.MeResponseRolesInner;
import co.sena.iam.contract.model.TokenResponse;
import co.sena.iam.contract.model.TokenResponseUser;
import co.sena.iam.domain.model.RoleAssignment;
import co.sena.iam.domain.model.User;
import co.sena.iam.domain.model.UserAccess;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HU-IAM-001 — {@code POST /auth/login} (E1/E2/E3) and {@code GET /auth/me} (E4).
 *
 * <p>Implements the generated {@link AuthApi}; the operations this HU does not cover keep the
 * interface's default {@code 501}. The class-level mapping supplies the {@code /api/v1} base
 * path declared by the contract's {@code servers} entry, without moving the probes the
 * template exposes at the root.
 */
@RestController
@RequestMapping("/api/v1")
class AuthController implements AuthApi {

    private static final int IP_COLUMN_LENGTH = 45;
    private static final int DEVICE_HINT_COLUMN_LENGTH = 200;

    private final AuthenticateUserUseCase authenticateUser;
    private final GetCurrentUserUseCase getCurrentUser;
    private final HttpServletRequest request;

    AuthController(
            AuthenticateUserUseCase authenticateUser,
            GetCurrentUserUseCase getCurrentUser,
            HttpServletRequest request) {
        this.authenticateUser = authenticateUser;
        this.getCurrentUser = getCurrentUser;
        this.request = request;
    }

    @Override
    public ResponseEntity<TokenResponse> login(LoginRequest loginRequest) {
        AuthenticationResult result = authenticateUser.authenticate(new LoginCommand(
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                clientIp(),
                header("User-Agent"),
                truncate(header("X-Device-Hint"), DEVICE_HINT_COLUMN_LENGTH)));

        return ResponseEntity.ok(new TokenResponse()
                .accessToken(result.accessToken())
                .refreshToken(result.refreshToken())
                .tokenType(TokenResponse.TokenTypeEnum.BEARER)
                .expiresIn(Math.toIntExact(result.expiresInSeconds()))
                .user(authUser(result.user(), result.access())));
    }

    @Override
    public ResponseEntity<MeResponse> getMe() {
        AuthenticatedIdentity identity = getCurrentUser.currentUser(BearerToken.from(request));
        User user = identity.user();
        UserAccess access = identity.access();

        return ResponseEntity.ok(new MeResponse()
                .id(user.id())
                .email(user.email())
                .fullName(user.fullName())
                .actorType(MeResponse.ActorTypeEnum.fromValue(user.actorType().name()))
                .actorId(user.actorId())
                .roles(roles(access))
                .features(access.featureCodes())
                .modules(access.moduleCodes()));
    }

    private static TokenResponseUser authUser(User user, UserAccess access) {
        return new TokenResponseUser()
                .id(user.id())
                .email(user.email())
                .fullName(user.fullName())
                .roles(access.roleNames())
                .trainingCenterId(access.primaryTrainingCenterId())
                .features(access.featureCodes());
    }

    private static List<MeResponseRolesInner> roles(UserAccess access) {
        return access.roles().stream()
                .map(AuthController::role)
                .toList();
    }

    private static MeResponseRolesInner role(RoleAssignment assignment) {
        return new MeResponseRolesInner()
                .name(assignment.roleName())
                .trainingCenterId(assignment.trainingCenterId());
    }

    /** First hop of {@code X-Forwarded-For} when the gateway set it, the socket peer otherwise. */
    private String clientIp() {
        String forwarded = header("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return truncate(forwarded.split(",")[0].trim(), IP_COLUMN_LENGTH);
        }
        return truncate(request.getRemoteAddr(), IP_COLUMN_LENGTH);
    }

    private String header(String name) {
        return request.getHeader(name);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
