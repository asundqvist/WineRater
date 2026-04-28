package se.edugrade.winerater.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    public Rating createRating(double score, String comment, Long wineId, Long userId) {
        Wine wine = wineRepository.findById(wineId)
                .orElseThrow(() -> new IllegalArgumentException("Wine with id [" + wineId + "] not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with id [" + userId + "] not found"));

        Rating rating = new Rating(score, comment, wine, user);
        return ratingRepository.save(rating);
    }
    public List<Rating> getRatingsByWine(Long wineId) {
        return ratingRepository.findByWineId(wineId);
    }
}
