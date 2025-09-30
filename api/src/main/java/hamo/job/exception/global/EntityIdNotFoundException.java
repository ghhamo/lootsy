package hamo.job.exception.global;

public class EntityIdNotFoundException extends ValidationException {
    @java.io.Serial
    private static final long serialVersionUID = -209583823754824159L;

    private final Long id;

    public EntityIdNotFoundException(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
