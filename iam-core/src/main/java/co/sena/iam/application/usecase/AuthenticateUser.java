package co.sena.iam.application.usecase;

import co.sena.iam.application.port.in.AuthenticateUserUseCase;
import co.sena.iam.application.port.out.UserRepository;

public final class AuthenticateUser implements AuthenticateUserUseCase {
    private final UserRepository users;

    public AuthenticateUser(UserRepository users) {
        this.users = users;
    }

    @Override
    public boolean authenticate(String email, String password) {
        // Skeleton: la impl real verifica el hash de password y locked_until.
        return users.findByEmail(email).filter(u -> u.isActive()).isPresent();
    }
}
