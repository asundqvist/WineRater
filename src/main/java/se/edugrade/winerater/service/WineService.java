package se.edugrade.winerater.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.exception.ResourceNotFoundException;
import se.edugrade.winerater.model.Wine;
import se.edugrade.winerater.model.Rating;
import se.edugrade.winerater.repositories.RatingRepository;
import se.edugrade.winerater.repositories.WineRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WineService {

    private final WineRepository wineRepository;
    private final RatingRepository ratingRepository;

    public record WineSummary(Wine wine, double averageRating, int ratingCount) {
    }

    public Wine getWineById(Long id) {
        return wineRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wine with id [" + id + "] not found"));
    }

    public List<Wine> getAllWines() {
        return wineRepository.findAll();
    }

    public List<WineSummary> getAllWineSummaries() {
        List<Wine> wines = wineRepository.findAll();
        if (wines.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> wineIds = wines.stream()
                .map(Wine::getId)
                .toList();

        Map<Long, List<Rating>> ratingsByWineId = ratingRepository.findByWineIdIn(wineIds)
                .stream()
                .collect(Collectors.groupingBy(rating -> rating.getWine().getId()));

        return wines.stream()
                .map(wine -> {
                    List<Rating> ratings = ratingsByWineId.getOrDefault(wine.getId(), Collections.emptyList());
                    double average = ratings.stream()
                            .mapToDouble(Rating::getScore)
                            .average()
                            .orElse(0.0);

                    return new WineSummary(wine, average, ratings.size());
                })
                .toList();
    }

    public Wine getWineByIdWithRatings(Long id) {
        return wineRepository.findWithRatingsById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wine with id [" + id + "] not found"));
    }

    public Wine createWine(Wine wine) {
        if (wineRepository.existsByNameIgnoreCase(wine.getName())) {
            throw new BadRequestException("Wine with name [" + wine.getName() + "] already exists");
        }

        return wineRepository.save(wine);
    }

    public Wine updateWine(Long id, Wine updatedWine) {
        Wine existingWine = getWineById(id);

        wineRepository.findByNameIgnoreCase(updatedWine.getName())
                .filter(wine -> !wine.getId().equals(id))
                .ifPresent(wine -> {
                    throw new BadRequestException("Wine with name [" + updatedWine.getName() + "] already exists");
                });

        existingWine.setName(updatedWine.getName());
        existingWine.setType(updatedWine.getType());
        existingWine.setCountry(updatedWine.getCountry());
        existingWine.setYear(updatedWine.getYear());

        return wineRepository.save(existingWine);
    }

    public Wine deleteWine(Long id) {
        Wine wine = getWineById(id);
        wineRepository.delete(wine);
        return wine;
    }

    public double getAverageRating(Long wineId) {
        Double average = ratingRepository.findAverageScoreByWineId(wineId);
        return average != null ? average : 0.0;
    }

    public int getRatingCount(Long wineId) {
        return ratingRepository.countByWineId(wineId);
    }
}
