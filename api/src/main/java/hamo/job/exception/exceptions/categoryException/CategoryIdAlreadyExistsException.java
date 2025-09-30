package hamo.job.exception.exceptions.categoryException;

import hamo.job.exception.global.EntityIdAlreadyExistsException;

public class CategoryIdAlreadyExistsException extends EntityIdAlreadyExistsException {
    public CategoryIdAlreadyExistsException(Long id) {
        super(id);
    }
}
