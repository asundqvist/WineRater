package se.edugrade.winerater.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import se.edugrade.winerater.model.Wine;
import se.edugrade.winerater.repositories.RatingRepository;
import se.edugrade.winerater.repositories.UserRepository;
import se.edugrade.winerater.repositories.WineRepository;
import se.edugrade.winerater.security.JwtUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:winerater-integration-test;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "jwt.secret=IntegrationTestSecretKeyForJwtMustBeAtLeast256BitsLong1234567890"
})
class WineRaterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WineRepository wineRepository;

    @BeforeEach
    void cleanDatabase() {
        ratingRepository.deleteAll();
        wineRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void userCanRegisterCreateWineRateItAndReadWineDetails() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User successfully registered!"));

        String userToken = bearerToken("alice", "ROLE_USER");

        mockMvc.perform(post("/api/wines")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Sancerre",
                                  "type": "White",
                                  "country": "France",
                                  "year": 2020
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sancerre"))
                .andExpect(jsonPath("$.averageRating").value(0.0))
                .andExpect(jsonPath("$.ratingCount").value(0));

        Wine wine = wineRepository.findByNameIgnoreCase("sancerre").orElseThrow();

        mockMvc.perform(post("/api/ratings")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "score": 4.5,
                                  "comment": "Fresh and balanced",
                                  "wineId": %d
                                }
                                """.formatted(wine.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(4.5))
                .andExpect(jsonPath("$.comment").value("Fresh and balanced"))
                .andExpect(jsonPath("$.username").value("alice"));

        mockMvc.perform(get("/api/wines/{id}", wine.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sancerre"))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.ratings[0].comment").value("Fresh and balanced"))
                .andExpect(jsonPath("$.ratings[0].username").value("alice"));

        assertThat(ratingRepository.countByWineId(wine.getId())).isEqualTo(1);
    }

    @Test
    void protectedEndpointsRequireAuthenticationAndRespectAdminRole() throws Exception {
        Wine wine = wineRepository.save(new Wine("Rioja", "Red", "Spain", 2019));

        mockMvc.perform(post("/api/wines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unauthenticated Wine",
                                  "type": "Red",
                                  "country": "Italy",
                                  "year": 2021
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Not authorized"));

        mockMvc.perform(delete("/api/wines/{id}", wine.getId())
                        .header("Authorization", bearerToken("alice", "ROLE_USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden action"));

        mockMvc.perform(put("/api/wines/{id}", wine.getId())
                        .header("Authorization", bearerToken("admin", "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Rioja Reserva",
                                  "type": "Red",
                                  "country": "Spain",
                                  "year": 2020
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rioja Reserva"))
                .andExpect(jsonPath("$.year").value(2020));

        mockMvc.perform(delete("/api/wines/{id}", wine.getId())
                        .header("Authorization", bearerToken("admin", "ROLE_ADMIN")))
                .andExpect(status().isNoContent());

        assertThat(wineRepository.findById(wine.getId())).isEmpty();
    }

    @Test
    void duplicateWineNameReturnsBadRequestThroughFullStack() throws Exception {
        String userToken = bearerToken("alice", "ROLE_USER");
        wineRepository.save(new Wine("Barolo", "Red", "Italy", 2018));

        mockMvc.perform(post("/api/wines")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "barolo",
                                  "type": "Red",
                                  "country": "Italy",
                                  "year": 2019
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Wine with name [barolo] already exists"));
    }

    private String bearerToken(String username, String role) {
        return "Bearer " + jwtUtil.generateAccessToken(username, List.of(role));
    }
}
