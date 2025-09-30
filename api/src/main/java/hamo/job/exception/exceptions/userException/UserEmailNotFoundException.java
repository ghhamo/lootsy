package hamo.job.exception.exceptions.userException;

import hamo.job.exception.global.EntityNameNotFoundException;

public class UserEmailNotFoundException extends EntityNameNotFoundException {
    public UserEmailNotFoundException(String string) {
        super(string);
    }
}
