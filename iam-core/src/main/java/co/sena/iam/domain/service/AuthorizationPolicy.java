package co.sena.iam.domain.service;

import co.sena.iam.domain.model.AuthorizationDecision;
import co.sena.iam.domain.model.AuthorizationProfile;
import co.sena.iam.domain.model.DecisionReason;
import co.sena.iam.domain.model.FeatureGrant;
import co.sena.iam.domain.model.ScopeOverrideRecord;
import co.sena.iam.domain.model.ScopeType;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * HU-IAM-006 — decides whether a user may exercise a feature against an optional target
 * training center, combining role-derived grants with per-user overrides.
 *
 * <p>Precedence (RN-IAM-03):
 * <ol>
 *   <li>An active block override for the feature denies it outright — an individual
 *       revocation that wins over every grant.</li>
 *   <li>Otherwise a role grant authorizes when its scope covers the target: {@code GLOBAL}
 *       covers any center; {@code TRAINING_CENTER} covers only its own center.</li>
 *   <li>Otherwise an active allow override authorizes: {@code GLOBAL} covers any center;
 *       {@code TRAINING_CENTER} covers only centers the user already belongs to (the
 *       override adds the feature, not new centers).</li>
 *   <li>Otherwise the request is denied.</li>
 * </ol>
 *
 * <p>Resource-level scopes ({@code AREA}, {@code OWN_*}) are not center-authorizable here;
 * they are enforced by the owning service and never grant a center-targeted request.
 */
public final class AuthorizationPolicy {

    public AuthorizationDecision decide(
            AuthorizationProfile profile, String featureCode, UUID targetCenterId, Instant now) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(featureCode, "featureCode");
        Objects.requireNonNull(now, "now");

        List<ScopeOverrideRecord> active = profile.overrides().stream()
                .filter(o -> o.featureCode().equals(featureCode))
                .filter(o -> o.isActiveAt(now))
                .toList();

        if (active.stream().anyMatch(o -> !o.allowed())) {
            return AuthorizationDecision.deny(DecisionReason.DENIED_BY_OVERRIDE);
        }

        if (roleGrantCovers(profile, featureCode, targetCenterId)) {
            return AuthorizationDecision.permit(DecisionReason.PERMITTED_BY_ROLE);
        }

        if (allowOverrideCovers(active, profile, targetCenterId)) {
            return AuthorizationDecision.permit(DecisionReason.PERMITTED_BY_OVERRIDE);
        }

        return AuthorizationDecision.deny(DecisionReason.DENIED_NO_GRANT);
    }

    private static boolean roleGrantCovers(
            AuthorizationProfile profile, String featureCode, UUID targetCenterId) {
        return profile.grants().stream()
                .filter(g -> g.featureCode().equals(featureCode))
                .anyMatch(g -> scopeCovers(g, targetCenterId));
    }

    private static boolean scopeCovers(FeatureGrant grant, UUID targetCenterId) {
        return switch (grant.scopeType()) {
            case GLOBAL -> true;
            case TRAINING_CENTER -> targetCenterId != null
                    && targetCenterId.equals(grant.trainingCenterId());
            default -> false;
        };
    }

    private static boolean allowOverrideCovers(
            List<ScopeOverrideRecord> active, AuthorizationProfile profile, UUID targetCenterId) {
        return active.stream()
                .filter(ScopeOverrideRecord::allowed)
                .anyMatch(o -> overrideCovers(o.scopeType(), profile, targetCenterId));
    }

    private static boolean overrideCovers(
            ScopeType scope, AuthorizationProfile profile, UUID targetCenterId) {
        return switch (scope) {
            case GLOBAL -> true;
            case TRAINING_CENTER -> targetCenterId != null
                    && profile.reachableCenters().contains(targetCenterId);
            default -> false;
        };
    }

}
