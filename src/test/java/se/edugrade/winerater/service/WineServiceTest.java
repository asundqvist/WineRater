package se.edugrade.winerater.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.edugrade.winerater.exception.BadRequestException;
import se.edugrade.winerater.exception.ResourceNotFoundException;
import se.edugrade.winerater.model.Rating;
import se.edugrade.winerater.model.Wine;
import se.edugrade.winerater.repositories.RatingRepository;
import se.edugrade.winerater.repositories.WineRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WineServiceTest {

    @Mock
    private WineRepository wineRepository;

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private WineService wineService;

    @Test
    void getWineByIdReturnsWineWhenFound() {
        Wine wine = wineWithId(1L, "Barolo");
        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));

        assertThat(wineService.getWineById(1L)).isSameAs(wine);
    }

    @Test
    void getWineByIdThrowsWhenMissing() {
        when(wineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wineService.getWineById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Wine with id [99] not found");
    }

    @Test
    void getAllWinesReturnsRepositoryResult() {
        List<Wine> wines = List.of(
                new Wine("Rioja", "Red", "Spain", 2019),
                new Wine("Chablis", "White", "France", 2021)
        );
        when(wineRepository.findAll()).thenReturn(wines);

        assertThat(wineService.getAllWines()).isSameAs(wines);
    }

    @Test
    void getAllWineSummariesCalculatesRatingStatsInMemory() {
        Wine rioja = wineWithId(1L, "Rioja");
        Wine chablis = wineWithId(2L, "Chablis");
        when(wineRepository.findAll()).thenReturn(List.of(rioja, chablis));
        when(ratingRepository.findByWineIdIn(List.of(1L, 2L))).thenReturn(List.of(
                new Rating(5.0, "Excellent", rioja, null),
                new Rating(3.0, "Good", rioja, null)
        ));

        assertThat(wineService.getAllWineSummaries())
                .extracting(WineService.WineSummary::averageRating, WineService.WineSummary::ratingCount)
                .containsExactly(
                        tuple(4.0, 2),
                        tuple(0.0, 0)
                );
    }

    @Test
    void getWineByIdWithRatingsReturnsWineWhenFound() {
        Wine wine = wineWithId(1L, "Barolo");
        when(wineRepository.findWithRatingsById(1L)).thenReturn(Optional.of(wine));

        assertThat(wineService.getWineByIdWithRatings(1L)).isSameAs(wine);
    }

    @Test
    void createWineSavesWhenNameIsUnique() {
        Wine wine = new Wine("Sancerre", "White", "France", 2020);
        when(wineRepository.existsByNameIgnoreCase("Sancerre")).thenReturn(false);
        when(wineRepository.save(wine)).thenReturn(wineWithId(10L, "Sancerre"));

        Wine created = wineService.createWine(wine);

        assertThat(created.getId()).isEqualTo(10L);
        verify(wineRepository).save(wine);
    }

    @Test
    void createWineThrowsWhenNameAlreadyExists() {
        Wine wine = new Wine("Sancerre", "White", "France", 2020);
        when(wineRepository.existsByNameIgnoreCase("Sancerre")).thenReturn(true);

        assertThatThrownBy(() -> wineService.createWine(wine))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Wine with name [Sancerre] already exists");
    }

    @Test
    void updateWineUpdatesExistingWineWhenNameIsAvailable() {
        Wine existing = wineWithId(1L, "Old Name");
        Wine updated = new Wine("New Name", "Red", "Italy", 2022);
        when(wineRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(wineRepository.findByNameIgnoreCase("New Name")).thenReturn(Optional.empty());
        when(wineRepository.save(existing)).thenReturn(existing);

        Wine result = wineService.updateWine(1L, updated);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getType()).isEqualTo("Red");
        assertThat(result.getCountry()).isEqualTo("Italy");
        assertThat(result.getYear()).isEqualTo(2022);
    }

    @Test
    void updateWineThrowsWhenAnotherWineHasRequestedName() {
        Wine existing = wineWithId(1L, "Old Name");
        Wine duplicate = wineWithId(2L, "New Name");
        Wine updated = new Wine("New Name", "Red", "Italy", 2022);
        when(wineRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(wineRepository.findByNameIgnoreCase("New Name")).thenReturn(Optional.of(duplicate));

        assertThatThrownBy(() -> wineService.updateWine(1L, updated))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Wine with name [New Name] already exists");
    }

    @Test
    void deleteWineDeletesAndReturnsWine() {
        Wine wine = wineWithId(1L, "Barolo");
        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));

        Wine deleted = wineService.deleteWine(1L);

        assertThat(deleted).isSameAs(wine);
        verify(wineRepository).delete(wine);
    }

    @Test
    void getAverageRatingReturnsZeroWhenNoRatingsExist() {
        when(ratingRepository.findAverageScoreByWineId(1L)).thenReturn(null);

        assertThat(wineService.getAverageRating(1L)).isZero();
    }

    @Test
    void getRatingCountReturnsRepositoryCount() {
        when(ratingRepository.countByWineId(1L)).thenReturn(3);

        assertThat(wineService.getRatingCount(1L)).isEqualTo(3);
    }

    private static Wine wineWithId(Long id, String name) {
        Wine wine = new Wine(name, "Red", "Italy", 2020);
        wine.setId(id);
        return wine;
    }
}
