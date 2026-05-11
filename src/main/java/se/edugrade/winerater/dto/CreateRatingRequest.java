package se.edugrade.winerater.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRatingRequest(
        @DecimalMin(value = "0.0", inclusive = true, message = "Score must be at least 0")
        @DecimalMax(value = "5.0", inclusive = true, message = "Score must be at most 5")
        double score,

        @Size(max = 200, message = "Max 200 characters")
        String comment,

        @NotNull(message = "Wine ID is required")
        Long wineId
) {
}
