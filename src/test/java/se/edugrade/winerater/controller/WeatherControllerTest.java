package se.edugrade.winerater.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.edugrade.winerater.dto.DailyWeatherResponse;
import se.edugrade.winerater.dto.WeatherResponse;
import se.edugrade.winerater.exception.ResourceNotFoundException;
import se.edugrade.winerater.security.JwtUtil;
import se.edugrade.winerater.service.WeatherService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
@AutoConfigureMockMvc(addFilters = false)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void getWeatherUsesToledoAsDefaultCity() throws Exception {
        when(weatherService.getWeatherForCity("Toledo")).thenReturn(weatherResponse("Toledo"));

        mockMvc.perform(get("/api/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Toledo"))
                .andExpect(jsonPath("$.country").value("Spain"))
                .andExpect(jsonPath("$.source").value("Open-Meteo"))
                .andExpect(jsonPath("$.dailyForecast[0].date").value("2026-05-08"));
    }

    @Test
    void getWeatherUsesRequestedCity() throws Exception {
        when(weatherService.getWeatherForCity("Stockholm")).thenReturn(weatherResponse("Stockholm"));

        mockMvc.perform(get("/api/weather").param("city", "Stockholm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Stockholm"))
                .andExpect(jsonPath("$.temperature").value(12.4));
    }

    @Test
    void getWeatherReturnsNotFoundWhenCityIsMissing() throws Exception {
        when(weatherService.getWeatherForCity("Missing"))
                .thenThrow(new ResourceNotFoundException("City [Missing] not found"));

        mockMvc.perform(get("/api/weather").param("city", "Missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("City [Missing] not found"));
    }

    private WeatherResponse weatherResponse(String city) {
        String country = "Toledo".equals(city) ? "Spain" : "Sweden";

        return new WeatherResponse(
                city,
                country,
                "2026-05-08T11:00",
                12.4,
                18.2,
                3,
                "Open-Meteo",
                List.of(new DailyWeatherResponse("2026-05-08", 7.2, 14.0, 3))
        );
    }
}
