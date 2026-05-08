package se.edugrade.winerater.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.exception.ResourceNotFoundException;
import se.edugrade.winerater.model.Rating;
import se.edugrade.winerater.model.User;
import se.edugrade.winerater.model.Wine;
import se.edugrade.winerater.repositories.RatingRepository;
import se.edugrade.winerater.repositories.UserRepository;
import se.edugrade.winerater.repositories.WineRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final WineRepository wineRepository;
    private final UserRepository userRepository;

    public Rating createRating(double score, String comment, Long wineId, String username) {
        Wine wine = wineRepository.findById(wineId)
                .orElseThrow(() -> new ResourceNotFoundException("Wine with id [" + wineId + "] not found"));

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username [" + username + "] not found"));

        if (ratingRepository.existsByWineIdAndUserId(wineId, user.getId())) {
            throw new BadRequestException("User has already rated this wine");
        }

        Rating rating = new Rating(score, comment, wine, user);
        return ratingRepository.save(rating);
    }

    public List<Rating> getRatingsByWine(Long wineId) {
        if (!wineRepository.existsById(wineId)) {
            throw new ResourceNotFoundException("Wine with id [" + wineId + "] not found");
        }

        return ratingRepository.findByWineId(wineId);
    }
}
