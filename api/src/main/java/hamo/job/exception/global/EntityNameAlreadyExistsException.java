package hamo.job.exception.global;

public class EntityNameAlreadyExistsException extends ValidationException {
    private final String name;

    public EntityNameAlreadyExistsException(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}