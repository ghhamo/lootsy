package hamo.job.exception.global;

public class EntityIdAlreadyExistsException extends ValidationException {
    private final Long id;

    public EntityIdAlreadyExistsException(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}