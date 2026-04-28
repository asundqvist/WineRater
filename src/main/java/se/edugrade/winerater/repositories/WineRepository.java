package se.edugrade.winerater.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.edugrade.winerater.model.Wine;

import java.util.List;

public interface WineRepository extends JpaRepository<Wine, Long> {

    List<Wine> findByNameContainingIgnoreCase(String name);
    List<Wine> findByTypeIgnoreCase(String type);
    List<Wine> findByCountryIgnoreCase(String country);
    List<Wine> findByYear(int year);
}
