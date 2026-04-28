package se.edugrade.winerater.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.edugrade.winerater.exception.ResourceNotFoundException;
import se.edugrade.winerater.model.Rating;
import se.edugrade.winerater.model.Wine;
import se.edugrade.winerater.repositories.RatingRepository;
import se.edugrade.winerater.repositories.WineRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WineService {

    private final WineRepository wineRepository;
    private final RatingRepository ratingRepository;

    public Wine getWineById(Long id) {
        return wineRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wine with id [" + id + "] not found"));
    }

    public List<Wine> getAllWines() {
        return wineRepository.findAll();
    }

    public Wine createWine(Wine wine) {
        return wineRepository.save(wine);
}

public Wine deleteWine(Long id) {
    Wine wine = getWineById(id);
    wineRepository.delete(wine);
    return wine;
}

public double getAverageRating(Long wineId) {
    Wine wine = getWineById(wineId);

    return wine.getRatings().stream()
            .mapToDouble(Rating::getScore)
            .average()
            .orElse(0.0);
}
}
