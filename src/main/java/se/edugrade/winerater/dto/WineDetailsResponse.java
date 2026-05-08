package se.edugrade.winerater.dto;

import java.util.List;

public record WineDetailsResponse(
        Long id,
        String name,
        String type,
        String country,
        int year,
        double averageRating,
        List<RatingResponse> ratings
) {
}
