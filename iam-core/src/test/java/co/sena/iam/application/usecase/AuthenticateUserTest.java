package co.sena.iam.application.usecase;

import static org.junit.jupiter.api.Assertions.assertTrue;

import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.domain.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthenticateUserTest {
    @Test
    void authenticatesActiveUser() {
        UserRepository repo = email -> Optional.of(new User("u1", email, true));
        AuthenticateUser uc = new AuthenticateUser(repo);
        assertTrue(uc.authenticate("a@b.co", "secret"));
    }
}
