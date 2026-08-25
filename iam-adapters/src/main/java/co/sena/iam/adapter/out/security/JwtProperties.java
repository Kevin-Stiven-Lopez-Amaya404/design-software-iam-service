package co.sena.iam.adapter.out.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Signing configuration. Bound from {@code IAM_JWT_*} environment variables; there is no
 * default for the key on purpose, so a missing secret stops the service at startup instead
 * of silently signing with something well known.
 *
 * @param privateKey RSA private key, PKCS#8, PEM or bare base64
 * @param issuer     {@code iss} claim, also required on verification
 * @param keyId      {@code kid} header, so keys can be rotated and published via JWKS later
 */
@ConfigurationProperties(prefix = "iam.jwt")
public record JwtProperties(String privateKey, String issuer, String keyId) {
}
