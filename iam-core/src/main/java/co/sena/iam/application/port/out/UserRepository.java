package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.User;
import java.util.Optional;

/** Puerto de salida (lo implementa el adapter de persistencia -> iam_db). */
public interface UserRepository {
    Optional<User> findByEmail(String email);
}
