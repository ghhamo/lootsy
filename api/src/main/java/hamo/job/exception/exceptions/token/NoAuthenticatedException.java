package hamo.job.exception.exceptions.token;

public class NoAuthenticatedException extends RuntimeException {
    public NoAuthenticatedException(String message) {
        super(message);
    }
}
