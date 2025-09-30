package hamo.job.exception.global;

public class EntityNameNotFoundException extends ValidationException {
    @java.io.Serial
    private static final long serialVersionUID = 230129027920973647L;

    private final String name;

    public EntityNameNotFoundException(String name) {
        this.name = name;
    }

    public String getUuid() {
        return name;
    }
}