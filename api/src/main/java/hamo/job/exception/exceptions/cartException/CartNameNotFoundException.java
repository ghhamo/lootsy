package hamo.job.exception.exceptions.cartException;

import hamo.job.exception.global.EntityNameNotFoundException;

public class CartNameNotFoundException extends EntityNameNotFoundException {
    public CartNameNotFoundException(String name) {
        super(name);
    }
}
