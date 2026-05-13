package se.edugrade.winerater.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.exception.ResourceNotFoundException;
import se.edugrade.winerater.model.Rating;
import se.edugrade.winerater.model.Role;
import se.edugrade.winerater.model.User;
import se.edugrade.winerater.model.Wine;
import se.edugrade.winerater.security.JwtUtil;
import se.edugrade.winerater.service.RatingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RatingService ratingService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void createRatingReturnsRatingResponseForAuthenticatedUser() throws Exception {
        Rating rating = ratingWithId(1L, 4.5, "Excellent", "alice");
        when(ratingService.createRating(4.5, "Excellent", 10L, "alice")).thenReturn(rating);

        mockMvc.perform(post("/api/ratings")
                        .principal(authentication("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "score": 4.5,
                                  "comment": "Excellent",
                                  "wineId": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.score").value(4.5))
                .andExpect(jsonPath("$.comment").value("Excellent"))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void createRatingReturnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/ratings")
                        .principal(authentication("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "score": 6.0,
                                  "comment": "Too high",
                                  "wineId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void createRatingReturnsBadRequestWhenUserAlreadyRatedWine() throws Exception {
        when(ratingService.createRating(4.0, "Good", 10L, "alice"))
                .thenThrow(new BadRequestException("User has already rated this wine"));

        mockMvc.perform(post("/api/ratings")
                        .principal(authentication("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "score": 4.0,
                                  "comment": "Good",
                                  "wineId": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User has already rated this wine"));
    }

    @Test
    void getRatingsByWineReturnsRatingResponses() throws Exception {
        Rating rating = ratingWithId(1L, 4.5, "Excellent", "alice");
        when(ratingService.getRatingsByWine(10L)).thenReturn(List.of(rating));

        mockMvc.perform(get("/api/ratings/wine/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].score").value(4.5))
                .andExpect(jsonPath("$[0].comment").value("Excellent"))
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    void getRatingsByWineReturnsNotFoundWhenWineIsMissing() throws Exception {
        when(ratingService.getRatingsByWine(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Wine with id [99] not found"));

        mockMvc.perform(get("/api/ratings/wine/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Wine with id [99] not found"));
    }

    @Test
    void deleteRatingCallsService() throws Exception {
        mockMvc.perform(delete("/api/ratings/1"))
                .andExpect(status().isNoContent());

        verify(ratingService).deleteRating(1L);
    }

    private static Rating ratingWithId(Long id, double score, String comment, String username) {
        Wine wine = new Wine("Rioja", "Red", "Spain", 2019);
        User user = new User(username, "password", Role.USER);
        Rating rating = new Rating(score, comment, wine, user);
        rating.setId(id);
        rating.setCreatedAt(LocalDateTime.of(2026, 5, 8, 9, 0));
        return rating;
    }

    private static Authentication authentication(String username) {
        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
