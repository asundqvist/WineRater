package se.edugrade.winerater.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateWineRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Type is required")
        String type,

        String grape,

        @NotBlank(message = "Country is required")
        String country,

        @Min(value = 1900, message = "Year must be 1900 or later")
        @Max(value = 2100, message = "Year must be reasonable")
        int year
) {
}
