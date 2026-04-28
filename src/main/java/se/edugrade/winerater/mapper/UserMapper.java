package se.edugrade.winerater.mapper;

import se.edugrade.winerater.dto.UserResponse;
import se.edugrade.winerater.model.User;

public class UserMapper {

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );
    }
}
