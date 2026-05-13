package se.edugrade.winerater.dto;

public record WineResponse(
        Long id,
        String name,
        String type,
        String grape,
        String country,
        int year,
        double averageRating,
        int ratingCount
) {
}
