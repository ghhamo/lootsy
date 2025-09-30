package hamo.job.exception.exceptions.userException;

import hamo.job.exception.global.EntityIdAlreadyExistsException;

public class UserIdAlreadyExistsException extends EntityIdAlreadyExistsException {
    public UserIdAlreadyExistsException(Long id) {
        super(id);
    }
}
