package se.edugrade.winerater.dto;

public record UserResponse(
        Long id,
        String username,
        String role
) {
}
