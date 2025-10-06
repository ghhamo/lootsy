package hamo.job.dto;

import hamo.job.entity.User;

import java.util.Objects;

public record CreateUserDTO(String name, String surname, String email, String password, String phoneNumber) {
    
    public CreateUserDTO {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(surname, "Surname cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(phoneNumber, "Phone number cannot be null");
    }

    public static User toUser(CreateUserDTO createUserDto) {
        User user = new User();
        user.setName(createUserDto.name);
        user.setSurname(createUserDto.surname);
        user.setEmail(createUserDto.email);
        user.setPassword(createUserDto.password);
        user.setPhoneNumber(createUserDto.phoneNumber);
        return user;
    }
}