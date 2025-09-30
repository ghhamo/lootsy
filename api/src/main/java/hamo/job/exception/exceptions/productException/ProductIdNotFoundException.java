package hamo.job.exception.exceptions.productException;

import hamo.job.exception.global.EntityIdNotFoundException;

public class ProductIdNotFoundException extends EntityIdNotFoundException {
    public ProductIdNotFoundException(Long id) {
        super(id);
    }
}
