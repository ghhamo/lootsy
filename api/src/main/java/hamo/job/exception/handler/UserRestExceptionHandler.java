package hamo.job.exception.handler;

import hamo.job.exception.exceptions.userException.UserEmailAlreadyExistsException;
import hamo.job.exception.exceptions.userException.UserEmailOrPasswordMismatchException;
import hamo.job.exception.exceptions.userException.UserIdAlreadyExistsException;
import hamo.job.exception.exceptions.userException.UserIdNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class UserRestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({UserIdNotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(UserIdNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("errors", Map.of(
                        "userId", "User not found",
                        "detail", ex.getMessage()
                )));
    }

    @ExceptionHandler({UserEmailAlreadyExistsException.class})
    public ResponseEntity<Object> handleAlreadyExistsRequest(UserEmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT) // 409
                .body(Map.of(
                        "errors", Map.of(
                                "userEmail", "User with this email already exists",
                                "detail", ex.getMessage())));
    }

    @ExceptionHandler({UserEmailOrPasswordMismatchException.class})
    public ResponseEntity<Object> handleWrongInputRequest(UserEmailOrPasswordMismatchException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401
                .body(Map.of(
                        "errors", Map.of(
                                "credentials", "Email or password is incorrect",
                                "detail", ex.getMessage())));
    }

    @ExceptionHandler({UserIdAlreadyExistsException.class})
    public ResponseEntity<Object> handleIdAlreadyExistsRequest(UserIdAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT) // 409
                .body(Map.of(
                        "errors", Map.of(
                                "userId", "User with this id already exists",
                                "detail", ex.getMessage())));
    }
}