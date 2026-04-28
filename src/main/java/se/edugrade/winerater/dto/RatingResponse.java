package se.edugrade.winerater.dto;

import java.time.LocalDateTime;

public record RatingResponse(
        Long id,
        double score,
        String comment,
        LocalDateTime createdAt,
        String username
) {
}
