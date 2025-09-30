package hamo.job.exception.exceptions.userException;

import hamo.job.exception.global.EntityNameAlreadyExistsException;

public class UserEmailAlreadyExistsException extends EntityNameAlreadyExistsException {
    public UserEmailAlreadyExistsException(String name) {
        super(name);
    }
}