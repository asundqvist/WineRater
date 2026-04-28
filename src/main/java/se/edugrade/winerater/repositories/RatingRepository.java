package se.edugrade.winerater.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.edugrade.winerater.model.Rating;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByWineId(Long wineId);
    List<Rating> findByUserId(Long userId);
    List<Rating> findByScoreIsGreaterThanEqual(double score);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.wine.id = :wineId")
    Double findAverageScoreByWineId(@Param("wineId") Long wineId);
}
