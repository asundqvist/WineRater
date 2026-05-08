package se.edugrade.winerater.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import se.edugrade.winerater.model.Wine;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WineRepository extends JpaRepository<Wine, Long> {

    List<Wine> findByNameContainingIgnoreCase(String name);
    List<Wine> findByTypeContainingIgnoreCase(String type);
    List<Wine> findByCountryContainingIgnoreCase(String country);
    List<Wine> findByYearBetween(int startYear, int endYear);
    Optional<Wine> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    @EntityGraph(attributePaths = {"ratings", "ratings.user"})
    @Query("SELECT w FROM Wine w WHERE w.id = :id")
    Optional<Wine> findWithRatingsById(@Param("id") Long id);
}
