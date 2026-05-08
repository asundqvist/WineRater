package se.edugrade.winerater.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

public record WeatherResponse(
        String city,
        String country,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        String time,

        double temperature,
        double windSpeed,
        int weatherCode,
        String source,
        List<DailyWeatherResponse> dailyForecast
) {
}
