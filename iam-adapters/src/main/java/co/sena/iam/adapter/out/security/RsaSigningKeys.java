package co.sena.iam.adapter.out.security;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * The RS256 key pair used to sign and verify access tokens. Only the private key is
 * configured; the public key is derived from it, which is what a JWKS endpoint will publish
 * once HU-IAM-006 lands.
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
class RsaSigningKeys {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String issuer;
    private final String keyId;

    RsaSigningKeys(JwtProperties properties) {
        if (properties.privateKey() == null || properties.privateKey().isBlank()) {
            throw new IllegalStateException(
                    "iam.jwt.private-key is required (set IAM_JWT_PRIVATE_KEY); refusing to start without a signing key");
        }
        if (properties.issuer() == null || properties.issuer().isBlank()) {
            throw new IllegalStateException("iam.jwt.issuer is required (set IAM_JWT_ISSUER)");
        }
        this.privateKey = readPrivateKey(properties.privateKey());
        this.publicKey = derivePublicKey(this.privateKey);
        this.issuer = properties.issuer();
        this.keyId = properties.keyId() == null || properties.keyId().isBlank() ? "iam-rsa-1" : properties.keyId();
    }

    PrivateKey privateKey() {
        return privateKey;
    }

    PublicKey publicKey() {
        return publicKey;
    }

    String issuer() {
        return issuer;
    }

    String keyId() {
        return keyId;
    }

    /** Accepts a PEM block or the bare base64 body, with or without line breaks. */
    private static PrivateKey readPrivateKey(String configured) {
        String base64 = configured
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        try {
            byte[] der = Base64.getDecoder().decode(base64);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(
                    "iam.jwt.private-key is not a PKCS#8 RSA private key (PEM or base64)", e);
        }
    }

    private static PublicKey derivePublicKey(PrivateKey privateKey) {
        if (!(privateKey instanceof RSAPrivateCrtKey crt)) {
            throw new IllegalStateException(
                    "iam.jwt.private-key must be an RSA CRT key so the public key can be derived");
        }
        try {
            return KeyFactory.getInstance("RSA")
                    .generatePublic(new RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Cannot derive the RSA public key", e);
        }
    }
}
