package co.sena.iam.adapter.in.rest;

import co.sena.iam.contract.model.Login400Response;
import co.sena.iam.contract.model.Login400ResponseErrorDetailsInner;
import co.sena.iam.domain.exception.AccountLockedException;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import co.sena.iam.domain.exception.UnauthenticatedException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps authentication failures onto the shared error envelope.
 *
 * <p>Messages stay generic on purpose: the answer to a login must not tell an attacker
 * whether an email exists.
 */
@RestControllerAdvice(assignableTypes = AuthController.class)
class AuthExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

    /** 423 Locked — the status the contract documents for {@code IAM_ACCOUNT_LOCKED}. */
    @ExceptionHandler(AccountLockedException.class)
    ResponseEntity<Login400Response> handleAccountLocked(
            AccountLockedException exception, HttpServletRequest request) {
        String traceId = ApiErrors.traceId(request);
        log.info("Login refused: account locked until {} (trace {})", exception.lockedUntil(), traceId);
        return ResponseEntity.status(HttpStatus.LOCKED).body(ApiErrors.of(
                IamErrorCode.ACCOUNT_LOCKED,
                "La cuenta está bloqueada por intentos fallidos. Intenta más tarde.",
                traceId));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<Login400Response> handleInvalidCredentials(HttpServletRequest request) {
        String traceId = ApiErrors.traceId(request);
        log.info("Login refused: invalid credentials (trace {})", traceId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrors.of(
                IamErrorCode.INVALID_CREDENTIALS,
                "Correo o contraseña incorrectos.",
                traceId));
    }

    @ExceptionHandler(UnauthenticatedException.class)
    ResponseEntity<Login400Response> handleUnauthenticated(HttpServletRequest request) {
        String traceId = ApiErrors.traceId(request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrors.of(
                IamErrorCode.UNAUTHENTICATED,
                "Token ausente, inválido o expirado.",
                traceId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Login400Response> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<Login400ResponseErrorDetailsInner> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> ApiErrors.detail(error.getField(), issueCodeOf(error.getCode())))
                .toList();
        return ResponseEntity.badRequest().body(ApiErrors.of(
                IamErrorCode.VALIDATION_ERROR,
                "El payload no es válido.",
                ApiErrors.traceId(request),
                details));
    }

    /** Bean-validation constraint name ("NotNull") as a language-independent issue code. */
    private static String issueCodeOf(String constraintName) {
        if (constraintName == null) {
            return "INVALID";
        }
        return constraintName.replaceAll("(?<=[a-z0-9])(?=[A-Z])", "_").toUpperCase(java.util.Locale.ROOT);
    }
}
