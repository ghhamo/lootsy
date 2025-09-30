package hamo.job.dto;

import hamo.job.entity.User;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record UserDTO(Long id, String name, String surname, String email,
                      String password, String phoneNumber) {
    
    public UserDTO {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(surname, "Surname cannot be null");
    }

    public static UserDTO fromUser(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getSurname(),
                user.getEmail(), user.getPassword(), user.getPhoneNumber());
    }

    public static User toUser(UserDTO userDto) {
        User user = new User();
        user.setName(userDto.name);
        user.setSurname(userDto.surname);
        user.setEmail(userDto.email);
        user.setPassword(userDto.password);
        user.setPhoneNumber(userDto.phoneNumber);
        return user;
    }

    public static Iterable<UserDTO> mapUserListToUserDtoList(Iterable<User> users) {
        Set<UserDTO> userDTOSet = new HashSet<>();
        for (User user : users) {
            userDTOSet.add(UserDTO.fromUser(user));
        }
        return userDTOSet;
    }
}