package hamo.job.exception.exceptions.categoryException;

import hamo.job.exception.global.EntityIdNotFoundException;

public class CategoryIdNotFoundException extends EntityIdNotFoundException {
    public CategoryIdNotFoundException(Long id) {
        super(id);
    }
}
