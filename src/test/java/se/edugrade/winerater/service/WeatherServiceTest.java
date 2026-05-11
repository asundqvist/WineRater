package se.edugrade.winerater.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import se.edugrade.winerater.dto.WeatherResponse;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeatherServiceTest {

    private MockRestServiceServer server;
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        weatherService = new WeatherService(restTemplate);
    }

    @Test
    void getWeatherForCityReturnsCurrentWeatherAndThreeDayForecast() {
        server.expect(requestTo(containsString("https://geocoding-api.open-meteo.com/v1/search")))
                .andExpect(queryParam("name", "stockholm"))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {
                              "name": "Stockholm",
                              "latitude": 59.3294,
                              "longitude": 18.0687,
                              "country": "Sweden"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo(containsString("https://api.open-meteo.com/v1/forecast")))
                .andExpect(queryParam("latitude", "59.3294"))
                .andExpect(queryParam("longitude", "18.0687"))
                .andExpect(queryParam("forecast_days", "3"))
                .andRespond(withSuccess("""
                        {
                          "current": {
                            "time": "2026-05-08T11:00",
                            "temperature_2m": 12.4,
                            "wind_speed_10m": 18.2,
                            "weather_code": 3
                          },
                          "daily": {
                            "time": ["2026-05-08", "2026-05-09", "2026-05-10"],
                            "weather_code": [3, 61, 1],
                            "temperature_2m_max": [14.0, 13.5, 15.2],
                            "temperature_2m_min": [7.2, 6.5, 8.1]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        WeatherResponse response = weatherService.getWeatherForCity("Stockholm");

        assertThat(response.city()).isEqualTo("Stockholm");
        assertThat(response.country()).isEqualTo("Sweden");
        assertThat(response.temperature()).isEqualTo(12.4);
        assertThat(response.source()).isEqualTo("Open-Meteo");
        assertThat(response.dailyForecast()).hasSize(3);
        assertThat(response.dailyForecast().getFirst().date()).isEqualTo("2026-05-08");

        server.verify();
    }

    @Test
    void getWeatherForCityUsesDefaultCityWhenInputIsBlank() {
        server.expect(requestTo(containsString("https://geocoding-api.open-meteo.com/v1/search")))
                .andExpect(queryParam("name", "toledo"))
                .andExpect(queryParam("count", "10"))
                .andExpect(queryParam("countryCode", "ES"))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {
                              "name": "Toledo",
                              "latitude": 41.6639,
                              "longitude": -83.5552,
                              "country": "United States",
                              "country_code": "US"
                            },
                            {
                              "name": "Toledo",
                              "latitude": 39.8581,
                              "longitude": -4.0226,
                              "country": "Spain",
                              "country_code": "ES"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo(containsString("https://api.open-meteo.com/v1/forecast")))
                .andRespond(withSuccess("""
                        {
                          "current": {
                            "time": "2026-05-08T11:00",
                            "temperature_2m": 11.0,
                            "wind_speed_10m": 12.0,
                            "weather_code": 2
                          },
                          "daily": {
                            "time": [],
                            "weather_code": [],
                            "temperature_2m_max": [],
                            "temperature_2m_min": []
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        WeatherResponse response = weatherService.getWeatherForCity(" ");

        assertThat(response.city()).isEqualTo("Toledo");
        server.verify();
    }

    @Test
    void getWeatherForCityThrowsWhenCityIsTooShort() {
        assertThatThrownBy(() -> weatherService.getWeatherForCity("A"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("City must contain at least 2 characters");
    }

    @Test
    void getWeatherForCityThrowsWhenCityIsMissing() {
        server.expect(requestTo(containsString("https://geocoding-api.open-meteo.com/v1/search")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> weatherService.getWeatherForCity("Unknown City"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("City [unknown city] not found");

        server.verify();
    }
}
