package co.sena.iam.adapter.in.rest;

/**
 * Stable error codes returned in the shared error envelope.
 *
 * <p>Cross-cutting codes come from {@code shared-contracts/errors/error-codes.yaml};
 * IAM-specific ones are prefixed with the domain, as that file requires.
 */
final class IamErrorCode {

    /** 401 — unknown email, wrong password, or an account that cannot be used. */
    static final String INVALID_CREDENTIALS = "IAM_INVALID_CREDENTIALS";

    /** 423 — the account is serving a lockout period (RN-IAM-01). */
    static final String ACCOUNT_LOCKED = "IAM_ACCOUNT_LOCKED";

    /** 401 — refresh token absent, expired or revoked. */
    static final String TOKEN_REVOKED = "IAM_TOKEN_REVOKED";

    /** 401 — bearer token missing, malformed or expired. */
    static final String UNAUTHENTICATED = "UNAUTHENTICATED";

    /** 404 — session not found for the given user. */
    static final String SESSION_NOT_FOUND = "IAM_SESSION_NOT_FOUND";

    /** 400 — payload rejected by validation. */
    static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    private IamErrorCode() {
    }
}
