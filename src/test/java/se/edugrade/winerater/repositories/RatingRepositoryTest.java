package se.edugrade.winerater.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;
import se.edugrade.winerater.model.Rating;
import se.edugrade.winerater.model.Role;
import se.edugrade.winerater.model.User;
import se.edugrade.winerater.model.Wine;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:rating-repository-test;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never"
})
class RatingRepositoryTest {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WineRepository wineRepository;

    @Test
    void findsRatingsByWineAndUser() {
        Wine rioja = wineRepository.save(new Wine("Rioja Crianza", "Red", "Spain", 2019));
        Wine chablis = wineRepository.save(new Wine("Chablis", "White", "France", 2021));
        User alice = userRepository.save(new User("alice", "{noop}password", Role.USER));
        User bob = userRepository.save(new User("bob", "{noop}password", Role.USER));

        ratingRepository.save(new Rating(4.5, "Excellent", rioja, alice));
        ratingRepository.save(new Rating(3.0, "Good", rioja, bob));
        ratingRepository.save(new Rating(4.0, "Fresh", chablis, alice));

        assertThat(ratingRepository.findByWineId(rioja.getId()))
                .extracting(Rating::getComment)
                .containsExactlyInAnyOrder("Excellent", "Good");

        assertThat(ratingRepository.findByUserId(alice.getId()))
                .extracting(Rating::getComment)
                .containsExactlyInAnyOrder("Excellent", "Fresh");
    }

    @Test
    void findsScoreThresholdCountAndAverageForWine() {
        Wine wine = wineRepository.save(new Wine("Brunello", "Red", "Italy", 2017));
        User alice = userRepository.save(new User("alice", "{noop}password", Role.USER));
        User bob = userRepository.save(new User("bob", "{noop}password", Role.USER));
        User clara = userRepository.save(new User("clara", "{noop}password", Role.USER));

        ratingRepository.save(new Rating(5.0, "Outstanding", wine, alice));
        ratingRepository.save(new Rating(3.0, "Fine", wine, bob));
        ratingRepository.save(new Rating(4.0, "Balanced", wine, clara));

        assertThat(ratingRepository.findByScoreIsGreaterThanEqual(4.0))
                .extracting(Rating::getComment)
                .containsExactlyInAnyOrder("Outstanding", "Balanced");

        assertThat(ratingRepository.countByWineId(wine.getId())).isEqualTo(3);
        assertThat(ratingRepository.findAverageScoreByWineId(wine.getId())).isEqualTo(4.0);
    }

    @Test
    void findsAndChecksRatingForWineAndUser() {
        Wine wine = wineRepository.save(new Wine("Sancerre", "White", "France", 2020));
        User user = userRepository.save(new User("alice", "{noop}password", Role.USER));
        User otherUser = userRepository.save(new User("bob", "{noop}password", Role.USER));

        ratingRepository.save(new Rating(4.0, "Crisp", wine, user));

        assertThat(ratingRepository.existsByWineIdAndUserId(wine.getId(), user.getId())).isTrue();
        assertThat(ratingRepository.existsByWineIdAndUserId(wine.getId(), otherUser.getId())).isFalse();
        assertThat(ratingRepository.findByWineIdAndUserId(wine.getId(), user.getId()))
                .isPresent()
                .get()
                .extracting(Rating::getComment)
                .isEqualTo("Crisp");
    }
}
