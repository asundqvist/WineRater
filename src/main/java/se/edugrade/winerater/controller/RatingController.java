package se.edugrade.winerater.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import se.edugrade.winerater.dto.CreateRatingRequest;
import se.edugrade.winerater.dto.RatingResponse;
import se.edugrade.winerater.mapper.RatingMapper;
import se.edugrade.winerater.model.Rating;
import se.edugrade.winerater.service.RatingService;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public RatingResponse createRating(@Valid @RequestBody CreateRatingRequest request) {
        Rating rating = ratingService.createRating(
                request.score(),
                request.comment(),
                request.wineId(),
                1L // TODO: Replace with actual user ID from authentication context
        );

        return RatingMapper.toRatingResponse(rating);
    }

    @GetMapping("/wine/{wineId}")
    public List<RatingResponse> getRatingsByWine(@PathVariable Long wineId) {
        return ratingService.getRatingsByWine(wineId)
                .stream()
                .map(RatingMapper::toRatingResponse)
                .toList();
    }
}
