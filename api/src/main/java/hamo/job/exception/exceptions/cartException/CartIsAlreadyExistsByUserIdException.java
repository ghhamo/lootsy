package hamo.job.exception.exceptions.cartException;

import hamo.job.exception.global.EntityIdAlreadyExistsException;

public class CartIsAlreadyExistsByUserIdException extends EntityIdAlreadyExistsException {
    public CartIsAlreadyExistsByUserIdException(Long id) {
        super(id);
    }
}
