package co.sena.iam.adapter.in.rest;

import co.sena.iam.application.port.in.GetCurrentUserUseCase;
import co.sena.iam.application.port.in.ListModulesUseCase;
import co.sena.iam.application.port.in.ListRoleFeaturesUseCase;
import co.sena.iam.application.port.in.ListRolesUseCase;
import co.sena.iam.contract.api.ModulesApi;
import co.sena.iam.contract.api.RolesApi;
import co.sena.iam.contract.model.Module;
import co.sena.iam.contract.model.ModuleFeaturesInner;
import co.sena.iam.contract.model.Role;
import co.sena.iam.contract.model.RoleFeature;
import co.sena.iam.domain.model.RbacModule;
import co.sena.iam.domain.model.RbacRole;
import co.sena.iam.domain.model.RoleFeatureEntry;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * HU-IAM-004 E1–E3 — RBAC catalog read endpoints.
 *
 * <p>Authentication: Bearer token validated by GetCurrentUserUseCase. Feature-level
 * authorization (IDENTITY_ROLE_VIEW) is enforced by HU-IAM-006.
 */
@RestController
@RequestMapping("/api/v1")
class RbacController implements ModulesApi, RolesApi {

    private final GetCurrentUserUseCase getCurrentUser;
    private final ListModulesUseCase listModules;
    private final ListRolesUseCase listRoles;
    private final ListRoleFeaturesUseCase listRoleFeatures;
    private final HttpServletRequest request;

    RbacController(
            GetCurrentUserUseCase getCurrentUser,
            ListModulesUseCase listModules,
            ListRolesUseCase listRoles,
            ListRoleFeaturesUseCase listRoleFeatures,
            HttpServletRequest request) {
        this.getCurrentUser = getCurrentUser;
        this.listModules = listModules;
        this.listRoles = listRoles;
        this.listRoleFeatures = listRoleFeatures;
        this.request = request;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    // ---------------------------------------------------------------- GET /modules

    @Override
    public ResponseEntity<List<Module>> listModules() {
        requireAuthenticated();
        List<Module> body = listModules.listModules().stream()
                .map(RbacController::toModule)
                .toList();
        return ResponseEntity.ok(body);
    }

    // ---------------------------------------------------------------- GET /roles

    @Override
    public ResponseEntity<List<Role>> listRoles() {
        requireAuthenticated();
        List<Role> body = listRoles.listRoles().stream()
                .map(RbacController::toRole)
                .toList();
        return ResponseEntity.ok(body);
    }

    // ---------------------------------------------------------------- GET /roles/{id}/features

    @Override
    public ResponseEntity<List<RoleFeature>> listRoleFeatures(UUID id) {
        requireAuthenticated();
        List<RoleFeature> body = listRoleFeatures.listRoleFeatures(id).stream()
                .map(RbacController::toRoleFeature)
                .toList();
        return ResponseEntity.ok(body);
    }

    // ---------------------------------------------------------------- auth guard

    /** Validates the Bearer token; throws UnauthenticatedException on any failure. */
    private void requireAuthenticated() {
        getCurrentUser.currentUser(BearerToken.from(request));
    }

    // ---------------------------------------------------------------- mappers

    private static Module toModule(RbacModule m) {
        return new Module()
                .code(m.code())
                .name(m.name())
                .description(m.description())
                .displayOrder(m.displayOrder())
                .iconKey(m.iconKey())
                .features(m.features().stream().map(RbacController::toFeature).toList());
    }

    private static ModuleFeaturesInner toFeature(co.sena.iam.domain.model.RbacFeature f) {
        return new ModuleFeaturesInner()
                .code(f.code())
                .name(f.name())
                .actionLevel(ModuleFeaturesInner.ActionLevelEnum.fromValue(f.actionLevel()));
    }

    private static Role toRole(RbacRole r) {
        return new Role()
                .id(r.id())
                .name(r.name())
                .displayName(r.displayName())
                .description(r.description())
                .isSystemRole(r.isSystemRole())
                .createdAt(OffsetDateTime.ofInstant(r.createdAt(), ZoneOffset.UTC));
    }

    private static RoleFeature toRoleFeature(RoleFeatureEntry e) {
        return new RoleFeature()
                .featureCode(e.featureCode())
                .featureName(e.featureName())
                .actionLevel(RoleFeature.ActionLevelEnum.fromValue(e.actionLevel()))
                .scopeType(RoleFeature.ScopeTypeEnum.fromValue(e.scopeType()));
    }
}
