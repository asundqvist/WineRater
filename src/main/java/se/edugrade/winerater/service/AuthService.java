package se.edugrade.winerater.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest req) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String token = jwtUtil.generateAccessToken(req.username(), roles);

        return new AuthResponse(token, req.username(), roles);
    }

    public void register(RegisterUserRequest req) {

        if (userRepository.existsByUsername(req.username())) {
            throw new BadRequestException("Username is already in use");
        }

        User user = new User(
                req.username(),
                passwordEncoder.encode(req.password()),
                Role.USER
        );

        userRepository.save(user);
    }
}
