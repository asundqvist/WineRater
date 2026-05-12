package se.edugrade.winerater.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.edugrade.winerater.dto.AuthRequest;
import se.edugrade.winerater.dto.AuthResponse;
import se.edugrade.winerater.dto.RegisterUserRequest;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.model.Role;
import se.edugrade.winerater.model.User;
import se.edugrade.winerater.repositories.UserRepository;
import se.edugrade.winerater.security.JwtUtil;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginAuthenticatesUserAndReturnsJwtResponse() {
        AuthRequest request = new AuthRequest("alice", "password");
        User user = new User("alice", "encoded-password", Role.USER);
        when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateAccessToken(eq("alice"), eq(List.of("ROLE_USER"))))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.roles()).containsExactly("ROLE_USER");
    }

    @Test
    void loginThrowsWhenUsernameDoesNotExist() {
        AuthRequest request = new AuthRequest("missing", "password");
        when(userRepository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username doesn't exist");
    }

    @Test
    void loginThrowsWhenPasswordIsIncorrect() {
        AuthRequest request = new AuthRequest("alice", "wrong-password");
        User user = new User("alice", "encoded-password", Role.USER);
        when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Incorrect password");
    }

    @Test
    void registerEncodesPasswordAndSavesUserWithUserRole() {
        RegisterUserRequest request = new RegisterUserRequest("alice", "plain-password");
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getUsername().equals("alice")
                        && user.getPassword().equals("encoded-password")
                        && user.getRole() == Role.USER
        ));
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExists() {
        RegisterUserRequest request = new RegisterUserRequest("alice", "plain-password");
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username is already in use");
    }
}
