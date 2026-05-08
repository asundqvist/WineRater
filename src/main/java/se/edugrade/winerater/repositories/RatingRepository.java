package se.edugrade.winerater.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.edugrade.winerater.model.Rating;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByWineId(Long wineId);
    List<Rating> findByWineIdIn(List<Long> wineIds);
    List<Rating> findByUserId(Long userId);
    List<Rating> findByScoreIsGreaterThanEqual(double score);
    boolean existsByWineIdAndUserId(Long wineId, Long userId);
    Optional<Rating> findByWineIdAndUserId(Long wineId, Long userId);
    int countByWineId(Long wineId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.wine.id = :wineId")
    Double findAverageScoreByWineId(@Param("wineId") Long wineId);
}
