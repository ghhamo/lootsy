package hamo.job.exception.exceptions.productException;

import hamo.job.exception.global.EntityIdAlreadyExistsException;

public class ProductIdAlreadyExistsException extends EntityIdAlreadyExistsException {
    public ProductIdAlreadyExistsException(Long id) {
        super(id);
    }
}
