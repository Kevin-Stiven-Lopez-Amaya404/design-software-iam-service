package co.sena.iam.domain.model;

import java.time.Duration;

/** Token lifetimes fixed by the contract: access 15 min, refresh 7 days. */
public final class TokenPolicy {

    public static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private TokenPolicy() {
    }
}
