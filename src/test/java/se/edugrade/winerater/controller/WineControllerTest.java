package se.edugrade.winerater.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.exception.ResourceNotFoundException;
import se.edugrade.winerater.model.Rating;
import se.edugrade.winerater.model.Role;
import se.edugrade.winerater.model.User;
import se.edugrade.winerater.model.Wine;
import se.edugrade.winerater.security.JwtUtil;
import se.edugrade.winerater.service.WineService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WineController.class)
@AutoConfigureMockMvc(addFilters = false)
class WineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WineService wineService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void getAllWinesReturnsWineResponsesWithRatingsSummary() throws Exception {
        Wine rioja = wineWithId(1L, "Rioja", "Red", "Spain", 2019);
        rioja.setGrape("Tempranillo");
        Wine chablis = wineWithId(2L, "Chablis", "White", "France", 2021);
        when(wineService.getAllWineSummaries()).thenReturn(List.of(
                new WineService.WineSummary(rioja, 4.5, 2),
                new WineService.WineSummary(chablis, 3.0, 1)
        ));

        mockMvc.perform(get("/api/wines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Rioja"))
                .andExpect(jsonPath("$[0].grape").value("Tempranillo"))
                .andExpect(jsonPath("$[0].averageRating").value(4.5))
                .andExpect(jsonPath("$[0].ratingCount").value(2))
                .andExpect(jsonPath("$[1].name").value("Chablis"));
    }

    @Test
    void getWineByIdReturnsDetailsWithRatings() throws Exception {
        Wine wine = wineWithId(1L, "Rioja", "Red", "Spain", 2019);
        Rating rating = ratingWithId(10L, 4.5, "Excellent", "alice");
        wine.getRatings().add(rating);
        when(wineService.getWineByIdWithRatings(1L)).thenReturn(wine);
        when(wineService.getAverageRating(1L)).thenReturn(4.5);

        mockMvc.perform(get("/api/wines/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rioja"))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.ratings[0].id").value(10))
                .andExpect(jsonPath("$.ratings[0].username").value("alice"));
    }

    @Test
    void getWineByIdReturnsNotFoundWhenServiceThrows() throws Exception {
        when(wineService.getWineByIdWithRatings(99L))
                .thenThrow(new ResourceNotFoundException("Wine with id [99] not found"));

        mockMvc.perform(get("/api/wines/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Wine with id [99] not found"));
    }

    @Test
    void createWineReturnsCreatedWineResponse() throws Exception {
        Wine saved = wineWithId(1L, "Sancerre", "White", "France", 2020);
        saved.setGrape("Sauvignon Blanc");
        when(wineService.createWine(any(Wine.class))).thenReturn(saved);

        mockMvc.perform(post("/api/wines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Sancerre",
                                  "type": "White",
                                  "grape": "Sauvignon Blanc",
                                  "country": "France",
                                  "year": 2020
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sancerre"))
                .andExpect(jsonPath("$.grape").value("Sauvignon Blanc"))
                .andExpect(jsonPath("$.averageRating").value(0.0))
                .andExpect(jsonPath("$.ratingCount").value(0));
    }

    @Test
    void createWineReturnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/wines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "type": "White",
                                  "country": "France",
                                  "year": 1800
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void createWineReturnsBadRequestWhenServiceRejectsDuplicate() throws Exception {
        when(wineService.createWine(any(Wine.class)))
                .thenThrow(new BadRequestException("Wine with name [Sancerre] already exists"));

        mockMvc.perform(post("/api/wines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Sancerre",
                                  "type": "White",
                                  "country": "France",
                                  "year": 2020
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wine with name [Sancerre] already exists"));
    }

    @Test
    void updateWineReturnsUpdatedWineResponse() throws Exception {
        Wine updated = wineWithId(1L, "Updated Rioja", "Red", "Spain", 2020);
        when(wineService.updateWine(eq(1L), any(Wine.class))).thenReturn(updated);
        when(wineService.getAverageRating(1L)).thenReturn(4.0);
        when(wineService.getRatingCount(1L)).thenReturn(3);

        mockMvc.perform(put("/api/wines/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Rioja",
                                  "type": "Red",
                                  "country": "Spain",
                                  "year": 2020
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Rioja"))
                .andExpect(jsonPath("$.averageRating").value(4.0))
                .andExpect(jsonPath("$.ratingCount").value(3));
    }

    @Test
    void deleteWineCallsService() throws Exception {
        mockMvc.perform(delete("/api/wines/1"))
                .andExpect(status().isNoContent());

        verify(wineService).deleteWine(1L);
    }

    private static Wine wineWithId(Long id, String name, String type, String country, int year) {
        Wine wine = new Wine(name, type, country, year);
        wine.setId(id);
        return wine;
    }

    private static Rating ratingWithId(Long id, double score, String comment, String username) {
        User user = new User(username, "password", Role.USER);
        Rating rating = new Rating(score, comment, new Wine("Rioja", "Red", "Spain", 2019), user);
        rating.setId(id);
        rating.setCreatedAt(LocalDateTime.of(2026, 5, 8, 9, 0));
        return rating;
    }
}
