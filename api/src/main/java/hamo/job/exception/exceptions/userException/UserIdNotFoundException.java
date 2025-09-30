package hamo.job.exception.exceptions.userException;

import hamo.job.exception.global.EntityIdNotFoundException;

public class UserIdNotFoundException extends EntityIdNotFoundException {
    public UserIdNotFoundException(Long id) {
        super(id);
    }
}
