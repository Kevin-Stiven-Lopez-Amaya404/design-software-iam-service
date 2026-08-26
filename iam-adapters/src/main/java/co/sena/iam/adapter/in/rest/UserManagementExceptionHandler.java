package co.sena.iam.adapter.in.rest;

import co.sena.iam.contract.model.Login400Response;
import co.sena.iam.domain.exception.EmailAlreadyExistsException;
import co.sena.iam.domain.exception.RoleAlreadyAssignedException;
import co.sena.iam.domain.exception.RoleNotFoundException;
import co.sena.iam.domain.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** HU-IAM-005 error mappings for user-management domain exceptions. */
@RestControllerAdvice(assignableTypes = {UserController.class, RbacController.class})
class UserManagementExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<Login400Response> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrors.of(
                IamErrorCode.USER_NOT_FOUND, ex.getMessage(), ApiErrors.traceId(request)));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    ResponseEntity<Login400Response> handleEmailConflict(
            EmailAlreadyExistsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrors.of(
                IamErrorCode.EMAIL_CONFLICT, ex.getMessage(), ApiErrors.traceId(request)));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    ResponseEntity<Login400Response> handleRoleNotFound(
            RoleNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrors.of(
                IamErrorCode.ROLE_NOT_FOUND, ex.getMessage(), ApiErrors.traceId(request)));
    }

    @ExceptionHandler(RoleAlreadyAssignedException.class)
    ResponseEntity<Login400Response> handleRoleConflict(
            RoleAlreadyAssignedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrors.of(
                IamErrorCode.ROLE_ALREADY_ASSIGNED, ex.getMessage(), ApiErrors.traceId(request)));
    }
}
