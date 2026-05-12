package se.edugrade.winerater.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;
import se.edugrade.winerater.model.Role;
import se.edugrade.winerater.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-repository-test;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsernameIgnoreCaseReturnsUser() {
        userRepository.save(new User("alice", "{noop}password", Role.USER));

        assertThat(userRepository.findByUsernameIgnoreCase("ALICE"))
                .isPresent()
                .get()
                .extracting(User::getRole)
                .isEqualTo(Role.USER);
    }

    @Test
    void existsByUsernameIgnoreCaseIgnoresCase() {
        userRepository.save(new User("AdminUser", "{noop}password", Role.ADMIN));

        assertThat(userRepository.existsByUsernameIgnoreCase("adminuser")).isTrue();
        assertThat(userRepository.existsByUsernameIgnoreCase("missing")).isFalse();
    }
}
