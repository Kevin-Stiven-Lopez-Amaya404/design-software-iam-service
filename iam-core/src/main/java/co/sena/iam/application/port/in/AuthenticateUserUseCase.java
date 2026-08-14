package co.sena.iam.application.port.in;

/** Puerto de entrada. */
public interface AuthenticateUserUseCase {
    boolean authenticate(String email, String password);
}
