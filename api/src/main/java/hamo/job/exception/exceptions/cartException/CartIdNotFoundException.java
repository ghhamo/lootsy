package hamo.job.exception.exceptions.cartException;

import hamo.job.exception.global.EntityIdNotFoundException;

public class CartIdNotFoundException extends EntityIdNotFoundException {
    public CartIdNotFoundException(Long id) {
        super(id);
    }
}
