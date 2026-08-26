package co.sena.iam.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.sena.iam.domain.model.AuthorizationDecision;
import co.sena.iam.domain.model.AuthorizationProfile;
import co.sena.iam.domain.model.DecisionReason;
import co.sena.iam.domain.model.FeatureGrant;
import co.sena.iam.domain.model.ScopeOverrideRecord;
import co.sena.iam.domain.model.ScopeType;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * HU-IAM-006 — the pure authorization decision. Combines role-derived feature grants
 * (rbac.user_role + rbac.role_feature) with per-user overrides (rbac.user_scope_override).
 */
class AuthorizationPolicyTest {

    private static final String FEATURE = "SCH_CREATE";
    private static final UUID CENTER_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CENTER_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-08-26T12:00:00Z");

    private final AuthorizationPolicy policy = new AuthorizationPolicy();

    // ------------------------------------------------------------------ E1: permitted by feature

    @Test
    @DisplayName("E1 a global role grant authorizes the feature in any center")
    void globalRoleGrantAuthorizesAnyCenter() {
        var profile = new AuthorizationProfile(
                List.of(new FeatureGrant(FEATURE, ScopeType.GLOBAL, null)),
                List.of());

        AuthorizationDecision decision = policy.decide(profile, FEATURE, CENTER_A, NOW);

        assertThat(decision.permitted()).isTrue();
        assertThat(decision.reason()).isEqualTo(DecisionReason.PERMITTED_BY_ROLE);
    }

    @Test
    @DisplayName("E1 a user without the feature is denied")
    void missingFeatureIsDenied() {
        var profile = new AuthorizationProfile(
                List.of(new FeatureGrant("OTHER_FEATURE", ScopeType.GLOBAL, null)),
                List.of());

        AuthorizationDecision decision = policy.decide(profile, FEATURE, CENTER_A, NOW);

        assertThat(decision.permitted()).isFalse();
        assertThat(decision.reason()).isEqualTo(DecisionReason.DENIED_NO_GRANT);
    }

    // ------------------------------------------------------------------ E2: scope by training center

    @Test
    @DisplayName("E2 a center-scoped grant authorizes inside the assigned center")
    void centerScopedGrantAuthorizesInsideCenter() {
        var profile = new AuthorizationProfile(
                List.of(new FeatureGrant(FEATURE, ScopeType.TRAINING_CENTER, CENTER_A)),
                List.of());

        AuthorizationDecision decision = policy.decide(profile, FEATURE, CENTER_A, NOW);

        assertThat(decision.permitted()).isTrue();
        assertThat(decision.reason()).isEqualTo(DecisionReason.PERMITTED_BY_ROLE);
    }

    @Test
    @DisplayName("E2 a center-scoped grant is denied outside the assigned center")
    void centerScopedGrantDeniedOutsideCenter() {
        var profile = new AuthorizationProfile(
                List.of(new FeatureGrant(FEATURE, ScopeType.TRAINING_CENTER, CENTER_A)),
                List.of());

        AuthorizationDecision decision = policy.decide(profile, FEATURE, CENTER_B, NOW);

        assertThat(decision.permitted()).isFalse();
        assertThat(decision.reason()).isEqualTo(DecisionReason.DENIED_NO_GRANT);
    }

    @Test
    @DisplayName("E2 a center-scoped grant is denied when the operation targets no center")
    void centerScopedGrantDeniedWithoutTargetCenter() {
        var profile = new AuthorizationProfile(
                List.of(new FeatureGrant(FEATURE, ScopeType.TRAINING_CENTER, CENTER_A)),
                List.of());

        AuthorizationDecision decision = policy.decide(profile, FEATURE, null, NOW);

        assertThat(decision.permitted()).isFalse();
    }

    // ------------------------------------------------------------------ E3: overrides

    @Test
    @DisplayName("E3 an allow override grants a feature the roles do not")
    void allowOverrideGrantsMissingFeature() {
        var profile = new AuthorizationProfile(
                List.of(),
                List.of(allowOverride(FEATURE, ScopeType.GLOBAL, null)));

        AuthorizationDecision decision = policy.decide(profile, FEATURE, CENTER_A, NOW);

        assertThat(decision.permitted()).isTrue();
        assertThat(decision.reason()).isEqualTo(DecisionReason.PERMITTED_BY_OVERRIDE);
    }

    @Test
    @DisplayName("E3 a block override revokes a feature the roles grant (deny wins)")
    void blockOverrideRevokesRoleGrant() {
        var profile = new AuthorizationProfile(
                List.of(new FeatureGrant(FEATURE, ScopeType.GLOBAL, null)),
                List.of(blockOverride(FEATURE, ScopeType.GLOBAL)));

        AuthorizationDecision decision = policy.decide(profile, FEATURE, CENTER_A, NOW);

        assertThat(decision.permitted()).isFalse();
        assertThat(decision.reason()).isEqualTo(DecisionReason.DENIED_BY_OVERRIDE);
    }

    @Test
    @DisplayName("E3 an expired allow override does not grant access")
    void expiredAllowOverrideIsIgnored() {
        var expired = new ScopeOverrideRecord(
                UUID.randomUUID(), FEATURE, ScopeType.GLOBAL, true, "temporary",
                UUID.randomUUID(), OffsetDateTime.ofInstant(NOW.minusSeconds(60), ZoneOffset.UTC),
                OffsetDateTime.ofInstant(NOW.minusSeconds(3600), ZoneOffset.UTC));
        var profile = new AuthorizationProfile(List.of(), List.of(expired));

        AuthorizationDecision decision = policy.decide(profile, FEATURE, CENTER_A, NOW);

        assertThat(decision.permitted()).isFalse();
        assertThat(decision.reason()).isEqualTo(DecisionReason.DENIED_NO_GRANT);
    }

    @Test
    @DisplayName("E3 a center-scoped allow override grants the feature within centers the user belongs to")
    void centerAllowOverrideBoundToUserCenters() {
        var profile = new AuthorizationProfile(
                List.of(new FeatureGrant("SOME_OTHER", ScopeType.TRAINING_CENTER, CENTER_A)),
                List.of(allowOverride(FEATURE, ScopeType.TRAINING_CENTER, null)));

        assertThat(policy.decide(profile, FEATURE, CENTER_A, NOW).permitted()).isTrue();
        assertThat(policy.decide(profile, FEATURE, CENTER_B, NOW).permitted()).isFalse();
    }

    // ------------------------------------------------------------------ helpers

    private static ScopeOverrideRecord allowOverride(String feature, ScopeType scope, OffsetDateTime expiresAt) {
        return new ScopeOverrideRecord(
                UUID.randomUUID(), feature, scope, true, "exceptional access",
                UUID.randomUUID(), expiresAt, OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC));
    }

    private static ScopeOverrideRecord blockOverride(String feature, ScopeType scope) {
        return new ScopeOverrideRecord(
                UUID.randomUUID(), feature, scope, false, "explicit block",
                UUID.randomUUID(), null, OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC));
    }
}
