package se.edugrade.winerater.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.edugrade.winerater.dto.CreateWineRequest;
import se.edugrade.winerater.dto.WineDetailsResponse;
import se.edugrade.winerater.dto.WineResponse;
import se.edugrade.winerater.mapper.WineMapper;
import se.edugrade.winerater.model.Wine;
import se.edugrade.winerater.service.WineService;

import java.util.List;

@RestController
@RequestMapping("/api/wines")
@RequiredArgsConstructor
public class WineController {

    private final WineService wineService;

    @GetMapping
    public List<WineResponse> getAllWines() {
        return wineService.getAllWineSummaries()
                .stream()
                .map(wine -> WineMapper.toWineResponse(
                        wine.wine(),
                        wine.averageRating(),
                        wine.ratingCount()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public WineDetailsResponse getWineById(@PathVariable Long id) {
        Wine wine = wineService.getWineByIdWithRatings(id);
        double avg = wineService.getAverageRating(id);
        return WineMapper.toWineDetailsResponse(wine, avg);
    }

    @PostMapping
    public ResponseEntity<WineResponse> createWine(@Valid @RequestBody CreateWineRequest request) {
        Wine wine = WineMapper.toWine(request);
        Wine saved = wineService.createWine(wine);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WineMapper.toWineResponse(saved, 0.0, 0));
    }

    @PutMapping("/{id}")
    public WineResponse updateWine(@PathVariable Long id, @Valid @RequestBody CreateWineRequest request) {
        Wine wine = WineMapper.toWine(request);
        Wine updated = wineService.updateWine(id, wine);

        return WineMapper.toWineResponse(
                updated,
                wineService.getAverageRating(updated.getId()),
                wineService.getRatingCount(updated.getId())
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWine(@PathVariable Long id) {
        wineService.deleteWine(id);
    }
}
