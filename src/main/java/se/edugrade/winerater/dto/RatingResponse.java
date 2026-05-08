package se.edugrade.winerater.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record RatingResponse(
        Long id,
        double score,
        String comment,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        String username
) {
}
