package se.edugrade.winerater.mapper;

import se.edugrade.winerater.dto.RatingResponse;
import se.edugrade.winerater.model.Rating;

public class RatingMapper {

    public static RatingResponse toRatingResponse(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getUser().getUsername()
        );
    }
}
