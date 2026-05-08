package se.edugrade.winerater.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;
import se.edugrade.winerater.model.Wine;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:wine-repository-test;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class WineRepositoryTest {

    @Autowired
    private WineRepository wineRepository;

    @Test
    void findByNameContainingIgnoreCaseReturnsMatchingWines() {
        wineRepository.save(new Wine("Barolo Riserva", "Red", "Italy", 2018));
        wineRepository.save(new Wine("Chablis Premier Cru", "White", "France", 2021));

        assertThat(wineRepository.findByNameContainingIgnoreCase("BAROLO"))
                .extracting(Wine::getName)
                .containsExactly("Barolo Riserva");
    }

    @Test
    void findByTypeCountryAndYearFiltersWines() {
        wineRepository.save(new Wine("Rioja Alta", "Red", "Spain", 2019));
        wineRepository.save(new Wine("Marlborough Classic", "White", "New Zealand", 2022));
        wineRepository.save(new Wine("Chianti Classico", "Red", "Italy", 2020));

        assertThat(wineRepository.findByTypeContainingIgnoreCase("red"))
                .extracting(Wine::getName)
                .containsExactlyInAnyOrder("Rioja Alta", "Chianti Classico");

        assertThat(wineRepository.findByCountryContainingIgnoreCase("new"))
                .extracting(Wine::getName)
                .containsExactly("Marlborough Classic");

        assertThat(wineRepository.findByYearBetween(2019, 2020))
                .extracting(Wine::getName)
                .containsExactlyInAnyOrder("Rioja Alta", "Chianti Classico");
    }

    @Test
    void findByNameIgnoreCaseAndExistsByNameIgnoreCaseIgnoreCase() {
        wineRepository.save(new Wine("Sancerre", "White", "France", 2020));

        assertThat(wineRepository.findByNameIgnoreCase("sAnCeRrE")).isPresent();
        assertThat(wineRepository.existsByNameIgnoreCase("SANCERRE")).isTrue();
        assertThat(wineRepository.existsByNameIgnoreCase("Unknown Wine")).isFalse();
    }
}
