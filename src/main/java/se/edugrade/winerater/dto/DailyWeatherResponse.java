package se.edugrade.winerater.dto;

public record DailyWeatherResponse(
        String date,
        double minTemperature,
        double maxTemperature,
        int weatherCode
) {
}
