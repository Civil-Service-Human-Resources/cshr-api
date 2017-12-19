package uk.gov.cshr.vcm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.Vacancy;

import java.util.List;
import java.util.Optional;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {

    default public Optional<Vacancy> findById(Long id) {
        return Optional.ofNullable(this.findOne(id));
    }

    /**
     * Entry point for search queries
     * Currently uses Postgres implementation
     * @param location user's input for the search on location
     * @param keyword user's input for the search on keyword
     * @return List of found vacancies
     */
    @Query(value = "SELECT * FROM vacancies WHERE location ILIKE %:location% AND CONCAT(title, ' ', description) ILIKE %:keyword%", nativeQuery = true)
    public List<Vacancy> search(@Param("location") String location, @Param("keyword") String keyword);

}
