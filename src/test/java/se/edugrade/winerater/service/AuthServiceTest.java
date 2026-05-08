package se.edugrade.winerater.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.edugrade.winerater.dto.AuthRequest;
import se.edugrade.winerater.dto.AuthResponse;
import se.edugrade.winerater.dto.RegisterUserRequest;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.model.Role;
import se.edugrade.winerater.model.User;
import se.edugrade.winerater.repositories.UserRepository;
import se.edugrade.winerater.security.JwtUtil;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginAuthenticatesUserAndReturnsJwtResponse() {
        AuthRequest request = new AuthRequest("alice", "password");
        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        doReturn(authorities).when(authentication).getAuthorities();
        when(jwtUtil.generateAccessToken(eq("alice"), eq(List.of("ROLE_USER", "ROLE_ADMIN"))))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.roles()).containsExactly("ROLE_USER", "ROLE_ADMIN");
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
