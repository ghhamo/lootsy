package hamo.job.exception.handler;

import hamo.job.exception.exceptions.userException.UserEmailAlreadyExistsException;
import hamo.job.exception.exceptions.userException.UserEmailOrPasswordMismatchException;
import hamo.job.exception.exceptions.userException.UserIdAlreadyExistsException;
import hamo.job.exception.exceptions.userException.UserIdNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class UserRestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({UserIdNotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(
            Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "{\"errors\":{\"userEmail\": \"User not found\"}}",
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({UserEmailAlreadyExistsException.class})
    public ResponseEntity<Object> handleAlreadyExistsRequest(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "{\"errors\":{\"userEmail\": \"User with email already exists\"}}",
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({UserEmailOrPasswordMismatchException.class})
    public ResponseEntity<Object> handleWrongInputRequest(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "{\"errors\":{\"userEmail\": \"User's email or password is incorrect\"}}",
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({UserIdAlreadyExistsException.class})
    public  ResponseEntity<Object> handleUUIDAlreadyExistsRequest(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "{\"errors\":{\"userId\": \"User with id already exists \"}}",
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}