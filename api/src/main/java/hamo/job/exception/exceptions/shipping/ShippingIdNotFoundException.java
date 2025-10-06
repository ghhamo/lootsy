package hamo.job.exception.exceptions.shipping;

import hamo.job.exception.global.EntityIdNotFoundException;

public class ShippingIdNotFoundException extends EntityIdNotFoundException {
    public ShippingIdNotFoundException(Long id) {
        super(id);
    }
}
