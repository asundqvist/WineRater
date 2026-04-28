package se.edugrade.winerater.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateRatingRequest(
        @DecimalMin(value = "0.0", inclusive = true, message = "Score must be at least 0.0")
        @DecimalMax(value = "5.0", inclusive = true, message = "Score must be at most 5.0")
        double score,

        String comment,

        @NotNull(message = "Wine ID is required")
        Long wineId
) {
}
