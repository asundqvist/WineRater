package se.edugrade.winerater.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.exception.ResourceNotFoundException;
import se.edugrade.winerater.model.Rating;
import se.edugrade.winerater.model.Role;
import se.edugrade.winerater.model.User;
import se.edugrade.winerater.model.Wine;
import se.edugrade.winerater.repositories.RatingRepository;
import se.edugrade.winerater.repositories.UserRepository;
import se.edugrade.winerater.repositories.WineRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private WineRepository wineRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RatingService ratingService;

    @Test
    void createRatingSavesRatingWhenWineUserAndRatingAreValid() {
        Wine wine = wineWithId(1L, "Rioja");
        User user = userWithId(2L, "alice");
        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));
        when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));
        when(ratingRepository.existsByWineIdAndUserId(1L, 2L)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rating created = ratingService.createRating(4.5, "Excellent", 1L, "alice");

        assertThat(created.getScore()).isEqualTo(4.5);
        assertThat(created.getComment()).isEqualTo("Excellent");
        assertThat(created.getWine()).isSameAs(wine);
        assertThat(created.getUser()).isSameAs(user);
        verify(ratingRepository).save(created);
    }

    @Test
    void createRatingThrowsWhenWineIsMissing() {
        when(wineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.createRating(4.0, "Good", 99L, "alice"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Wine with id [99] not found");
    }

    @Test
    void createRatingThrowsWhenUserIsMissing() {
        Wine wine = wineWithId(1L, "Rioja");
        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));
        when(userRepository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.createRating(4.0, "Good", 1L, "missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with username [missing] not found");
    }

    @Test
    void createRatingThrowsWhenUserAlreadyRatedWine() {
        Wine wine = wineWithId(1L, "Rioja");
        User user = userWithId(2L, "alice");
        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));
        when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));
        when(ratingRepository.existsByWineIdAndUserId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> ratingService.createRating(4.0, "Good", 1L, "alice"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User has already rated this wine");
    }

    @Test
    void getRatingsByWineReturnsRatingsWhenWineExists() {
        List<Rating> ratings = List.of(new Rating(4.0, "Good", wineWithId(1L, "Rioja"), userWithId(2L, "alice")));
        when(wineRepository.existsById(1L)).thenReturn(true);
        when(ratingRepository.findByWineId(1L)).thenReturn(ratings);

        assertThat(ratingService.getRatingsByWine(1L)).isSameAs(ratings);
    }

    @Test
    void getRatingsByWineThrowsWhenWineIsMissing() {
        when(wineRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> ratingService.getRatingsByWine(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Wine with id [99] not found");
    }

    @Test
    void deleteRatingDeletesWhenRatingExists() {
        Rating rating = new Rating(4.0, "Good", wineWithId(1L, "Rioja"), userWithId(2L, "alice"));
        when(ratingRepository.findById(10L)).thenReturn(Optional.of(rating));

        ratingService.deleteRating(10L);

        verify(ratingRepository).delete(rating);
    }

    @Test
    void deleteRatingThrowsWhenRatingIsMissing() {
        when(ratingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.deleteRating(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Rating with id [99] not found");
    }

    private static Wine wineWithId(Long id, String name) {
        Wine wine = new Wine(name, "Red", "Spain", 2019);
        wine.setId(id);
        return wine;
    }

    private static User userWithId(Long id, String username) {
        User user = new User(username, "encoded-password", Role.USER);
        user.setId(id);
        return user;
    }
}
