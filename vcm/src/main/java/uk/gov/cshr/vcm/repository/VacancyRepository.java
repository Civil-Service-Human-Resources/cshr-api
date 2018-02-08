package uk.gov.cshr.vcm.repository;

import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.Vacancy;

@Repository
@Transactional
public interface VacancyRepository extends PagingAndSortingRepository<Vacancy, Long>, VacancyRepositoryCustom {

    default Optional<Vacancy> findById(Long id) {
        return Optional.ofNullable(this.findOne(id));
    }

    /**
     * Entry point for search queries by location and keyword
     * Currently uses Postgres implementation
     *
     * @param location user's input for the search on location
     * @param keyword  user's input for the search on keyword
     * @return List of found vacancies
     */
    @Query(value = "SELECT * FROM vacancies WHERE location ILIKE %:location% AND CONCAT(title, ' ', description) ILIKE %:keyword% ORDER BY ?#{#pageable}",
            countQuery = "SELECT COUNT(*) FROM vacancies WHERE location ILIKE %:location% AND CONCAT(title, ' ', description) ILIKE %:keyword%",
            nativeQuery = true)
    Page<Vacancy> search(@Param("location") String location, @Param("keyword") String keyword, Pageable pageable);

    /**
     * Entry point for search queries by location only
     * Currently uses Postgres implementation
     *
     * @param location user's input for the search on location
     * @return List of found vacancies
     */
    @Query(value = "SELECT * FROM vacancies WHERE location ILIKE %:location%  ORDER BY ?#{#pageable}",
            countQuery = "SELECT COUNT(*) FROM vacancies WHERE location ILIKE %:location%",
            nativeQuery = true)
    Page<Vacancy> search(@Param("location") String location, Pageable pageable);
}
