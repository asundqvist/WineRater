package se.edugrade.winerater.mapper;

import se.edugrade.winerater.dto.CreateWineRequest;
import se.edugrade.winerater.dto.WineDetailsResponse;
import se.edugrade.winerater.dto.WineResponse;
import se.edugrade.winerater.model.Wine;

public class WineMapper {

    public static WineResponse toWineResponse(Wine wine, double averageRating, int ratingCount) {
        return new WineResponse(
                wine.getId(),
                wine.getName(),
                wine.getType(),
                wine.getGrape(),
                wine.getCountry(),
                wine.getYear(),
                averageRating,
                ratingCount
        );
    }

    public static WineDetailsResponse toWineDetailsResponse(Wine wine, double averageRating) {
        return new WineDetailsResponse(
                wine.getId(),
                wine.getName(),
                wine.getType(),
                wine.getGrape(),
                wine.getCountry(),
                wine.getYear(),
                averageRating,
                wine.getRatings().stream()
                        .map(RatingMapper::toRatingResponse)
                        .toList()
        );
    }

    public static Wine toWine(CreateWineRequest request) {
        return new Wine(
                request.name(),
                request.type(),
                blankToNull(request.grape()),
                request.country(),
                request.year()
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
