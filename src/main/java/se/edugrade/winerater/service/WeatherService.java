package se.edugrade.winerater.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import se.edugrade.winerater.dto.DailyWeatherResponse;
import se.edugrade.winerater.dto.WeatherResponse;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.exception.ExternalWeatherException;
import se.edugrade.winerater.exception.ResourceNotFoundException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final String DEFAULT_CITY = "Toledo";
    private static final String DEFAULT_COUNTRY_CODE = "ES";
    private static final String SOURCE = "Open-Meteo";
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";

    private final RestTemplate restTemplate;

    public WeatherResponse getWeatherForCity(String city) {
        String searchCity = normalizeCity(city);
        Location location = findLocation(searchCity);
        ForecastApiResponse forecast = fetchForecast(location);

        if (forecast == null || forecast.current() == null) {
            throw new ResourceNotFoundException("Weather for city [" + searchCity + "] not found");
        }

        CurrentWeather current = forecast.current();

        return new WeatherResponse(
                location.name(),
                location.country(),
                current.time(),
                current.temperature_2m(),
                current.wind_speed_10m(),
                current.weather_code(),
                SOURCE,
                toDailyForecast(forecast.daily())
        );
    }

    private String normalizeCity(String city) {
        if (city == null || city.isBlank()) {
            return DEFAULT_CITY.toLowerCase();
        }

        String trimmedCity = city.trim();
        if (trimmedCity.length() < 2) {
            throw new BadRequestException("City must contain at least 2 characters");
        }

        return trimmedCity.toLowerCase();
    }

    private Location findLocation(String city) {
        boolean shouldUseDefaultCountry = DEFAULT_CITY.equalsIgnoreCase(city);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(GEOCODING_URL)
                .queryParam("name", city)
                .queryParam("count", shouldUseDefaultCountry ? 10 : 1)
                .queryParam("language", "en")
                .queryParam("format", "json");

        if (shouldUseDefaultCountry) {
            uriBuilder.queryParam("countryCode", DEFAULT_COUNTRY_CODE);
        }

        URI uri = uriBuilder
                .build()
                .encode()
                .toUri();

        try {
            GeocodingApiResponse response = restTemplate.getForObject(uri, GeocodingApiResponse.class);
            if (response == null || response.results() == null || response.results().isEmpty()) {
                throw new ResourceNotFoundException("City [" + city + "] not found");
            }

            if (shouldUseDefaultCountry) {
                return response.results()
                        .stream()
                        .filter(location -> DEFAULT_COUNTRY_CODE.equalsIgnoreCase(location.country_code()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "City [" + city + "] not found in country [" + DEFAULT_COUNTRY_CODE + "]"
                        ));
            }

            return response.results().getFirst();
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ExternalWeatherException("Could not fetch location data from Open-Meteo", ex);
        }
    }

    private ForecastApiResponse fetchForecast(Location location) {
        URI uri = UriComponentsBuilder.fromUriString(FORECAST_URL)
                .queryParam("latitude", location.latitude())
                .queryParam("longitude", location.longitude())
                .queryParam("current", "temperature_2m,apparent_temperature,wind_speed_10m,weather_code")
                .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum")
                .queryParam("forecast_days", 3)
                .queryParam("timezone", "auto")
                .build()
                .encode()
                .toUri();

        try {
            return restTemplate.getForObject(uri, ForecastApiResponse.class);
        } catch (RestClientException ex) {
            throw new ExternalWeatherException("Could not fetch weather data from Open-Meteo", ex);
        }
    }

    private List<DailyWeatherResponse> toDailyForecast(DailyWeather daily) {
        if (daily == null || daily.time() == null) {
            return List.of();
        }

        List<DailyWeatherResponse> forecast = new ArrayList<>();
        for (int i = 0; i < daily.time().size(); i++) {
            forecast.add(new DailyWeatherResponse(
                    daily.time().get(i),
                    valueAt(daily.temperature_2m_min(), i),
                    valueAt(daily.temperature_2m_max(), i),
                    intValueAt(daily.weather_code(), i)
            ));
        }

        return forecast;
    }

    private double valueAt(List<Double> values, int index) {
        if (values == null || values.size() <= index || values.get(index) == null) {
            return 0.0;
        }

        return values.get(index);
    }

    private int intValueAt(List<Integer> values, int index) {
        if (values == null || values.size() <= index || values.get(index) == null) {
            return 0;
        }

        return values.get(index);
    }

    private record GeocodingApiResponse(List<Location> results) {
    }

    private record Location(String name, double latitude, double longitude, String country, String country_code) {
    }

    private record ForecastApiResponse(CurrentWeather current, DailyWeather daily) {
    }

    private record CurrentWeather(
            String time,
            double temperature_2m,
            double wind_speed_10m,
            int weather_code
    ) {
    }

    private record DailyWeather(
            List<String> time,
            List<Integer> weather_code,
            List<Double> temperature_2m_max,
            List<Double> temperature_2m_min
    ) {
    }
}
