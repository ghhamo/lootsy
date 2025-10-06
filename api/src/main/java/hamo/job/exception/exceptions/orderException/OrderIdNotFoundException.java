package hamo.job.exception.exceptions.orderException;

import hamo.job.exception.global.EntityIdNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderIdNotFoundException extends EntityIdNotFoundException {
    public OrderIdNotFoundException(Long id) {
        super(id);
    }
}
