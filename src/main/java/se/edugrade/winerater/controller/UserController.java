package se.edugrade.winerater.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import se.edugrade.winerater.dto.RegisterUserRequest;
import se.edugrade.winerater.dto.UserResponse;
import se.edugrade.winerater.mapper.UserMapper;
import se.edugrade.winerater.model.Role;
import se.edugrade.winerater.model.User;
import se.edugrade.winerater.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return UserMapper.toUserResponse(userService.getUserById(id));
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody RegisterUserRequest request) {
        User user = new User(
                request.username(),
                request.password(),
                Role.USER
        );

        return UserMapper.toUserResponse(userService.createUser(user));
    }
}
