package hamo.job.dto;

import java.util.Objects;

public record UpdateAccountDTO(String name, String surname, String phoneNumber) {
    
    public UpdateAccountDTO {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(surname, "Surname cannot be null");
        
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (surname.trim().isEmpty()) {
            throw new IllegalArgumentException("Surname cannot be empty");
        }
    }
}
