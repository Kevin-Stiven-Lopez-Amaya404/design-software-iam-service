package co.sena.iam.domain.exception;

import java.util.UUID;

public final class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(UUID id) {
        super("Role not found: " + id);
    }
}
