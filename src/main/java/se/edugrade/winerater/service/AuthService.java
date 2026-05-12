package se.edugrade.winerater.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.edugrade.winerater.dto.AuthRequest;
import se.edugrade.winerater.dto.AuthResponse;
import se.edugrade.winerater.dto.RegisterUserRequest;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.model.Role;
import se.edugrade.winerater.model.User;
import se.edugrade.winerater.repositories.UserRepository;
import se.edugrade.winerater.security.JwtUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest req) {

        User user = userRepository.findByUsernameIgnoreCase(req.username())
                .orElseThrow(() -> new BadRequestException("Username doesn't exist"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BadRequestException("Incorrect password");
        }

        List<String> roles = List.of("ROLE_" + user.getRole().name());
        String token = jwtUtil.generateAccessToken(user.getUsername(), roles);

        return new AuthResponse(token, user.getUsername(), roles);
    }

    public void register(RegisterUserRequest req) {

        if (userRepository.existsByUsernameIgnoreCase(req.username())) {
            throw new BadRequestException("Username is already in use");
        }

        User user = new User(
                req.username(),
                passwordEncoder.encode(req.password()),
                Role.USER
        );

        userRepository.save(user);
    }

    public boolean isUsernameAvailable(String username) {
        String trimmedUsername = username == null ? "" : username.trim();

        if (trimmedUsername.length() < 3) {
            return false;
        }

        return !userRepository.existsByUsernameIgnoreCase(trimmedUsername);
    }
}
