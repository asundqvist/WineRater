package se.edugrade.winerater.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        return wineService.getAllWines()
                .stream()
                .map(WineMapper::toWineResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public WineDetailsResponse getWineById(@PathVariable Long id) {
        Wine wine = wineService.getWineById(id);
        double avg = wineService.getAverageRating(id);
        return WineMapper.toWineDetailsResponse(wine, avg);
    }

    @PostMapping
    public WineResponse createWine(@Valid @RequestBody CreateWineRequest request) {
        Wine wine = WineMapper.toWine(request);
        Wine saved = wineService.createWine(wine);

        return WineMapper.toWineResponse(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteWine(@PathVariable Long id) {
        wineService.deleteWine(id);
    }
}
