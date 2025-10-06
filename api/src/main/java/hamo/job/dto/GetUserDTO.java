package hamo.job.dto;

import hamo.job.entity.User;

import java.util.HashSet;
import java.util.Set;

public record GetUserDTO(Long id, String name, String surname, String email, String phoneNumber) {

    public static GetUserDTO fromUser(User user) {
        return new GetUserDTO(user.getId(), user.getName(), user.getSurname(),
                user.getEmail(), user.getPhoneNumber());
    }

    public static Iterable<GetUserDTO> mapUserListToUserDtoList(Iterable<User> users) {
        Set<GetUserDTO> getUserDTOS = new HashSet<>();
        for (User user : users) {
            getUserDTOS.add(GetUserDTO.fromUser(user));
        }
        return getUserDTOS;
    }
}
