package hamo.job.dto;

import hamo.job.entity.User;

import java.util.Objects;

public record AccountDTO(
        Long id,
        String name,
        String surname,
        String email,
        String phoneNumber,
        boolean enabled,
        UserStatsDTO stats) {
    
    public AccountDTO {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(surname, "Surname cannot be null");
        Objects.requireNonNull(stats, "Stats cannot be null");
    }

    public static AccountDTO fromUser(User user, UserStatsDTO stats) {
        return new AccountDTO(
            user.getId(), 
            user.getName(), 
            user.getSurname(),
            user.getEmail(), 
            user.getPhoneNumber(), 
            user.isEnabled(),
            stats
        );
    }
}
