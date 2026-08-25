package co.sena.iam.application.port.out;

/** Outbound port for password hashing and verification (BCrypt in the adapter). */
public interface PasswordHasher {

    /** Constant-time comparison of a raw password against a stored hash. */
    boolean matches(String rawPassword, String passwordHash);

    /** Produces a salted hash of {@code rawPassword} suitable for storage. */
    String hash(String rawPassword);
}
