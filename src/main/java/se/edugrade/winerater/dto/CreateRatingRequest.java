package se.edugrade.winerater.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record CreateRatingRequest(
        @DecimalMin(value = "1.0", inclusive = true, message = "Score must be at least 1")
        @DecimalMax(value = "5.0", inclusive = true, message = "Score must be at most 5")
        int score,

        String comment,

        Long wineId
) {
}
