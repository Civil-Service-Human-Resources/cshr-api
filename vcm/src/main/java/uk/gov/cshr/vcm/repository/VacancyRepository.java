package uk.gov.cshr.vcm.repository;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.Vacancy;

@Repository
@Transactional
public interface VacancyRepository extends PagingAndSortingRepository<Vacancy, Long> {
    default Optional<Vacancy> findById(Long id) {
        return Optional.ofNullable(this.findOne(id));
    }

    /**
     * Finds a vacancy using the jobRef and vendorIdentifier as search criteria.
     *
     * jobRef and vendorIdentifier should be a unique combination in the vacancies table.
     *
     * @param jobRef Applicant Tracking System id to the vacancy
     * @param vendorIdentifier identifies which Applicant Tracking System is the source of the vacancy
     * @return list of vacancies that match the given search criteria. There should be 0 or 1 Vacancies in the list.
     */
    @Query("select v from Vacancy v where v.identifier = :jobRef and v.atsVendorIdentifier = :vendorIdentifier")
    List<Vacancy> findVacancy(@Param("jobRef") Long jobRef, @Param("vendorIdentifier") String vendorIdentifier);
}
