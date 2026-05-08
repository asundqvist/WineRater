package se.edugrade.winerater.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.exception.ResourceNotFoundException;
import se.edugrade.winerater.model.Role;
import se.edugrade.winerater.model.User;
import se.edugrade.winerater.repositories.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserByIdReturnsUserWhenFound() {
        User user = userWithId(1L, "alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userService.getUserById(1L)).isSameAs(user);
    }

    @Test
    void getUserByIdThrowsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id [99] not found");
    }

    @Test
    void getUserByUsernameReturnsUserWhenFound() {
        User user = userWithId(1L, "alice");
        when(userRepository.findByUsernameIgnoreCase("ALICE")).thenReturn(Optional.of(user));

        assertThat(userService.getUserByUsername("ALICE")).isSameAs(user);
    }

    @Test
    void createUserEncodesPasswordAndSavesUser() {
        User user = new User("alice", "plain-password", Role.USER);
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(userWithId(1L, "alice"));

        User created = userService.createUser(user);

        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(created.getId()).isEqualTo(1L);
        verify(userRepository).save(user);
    }

    @Test
    void createUserThrowsWhenUsernameAlreadyExists() {
        User user = new User("alice", "plain-password", Role.USER);
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User with username [alice] already exists");
    }

    private static User userWithId(Long id, String username) {
        User user = new User(username, "encoded-password", Role.USER);
        user.setId(id);
        return user;
    }
}
