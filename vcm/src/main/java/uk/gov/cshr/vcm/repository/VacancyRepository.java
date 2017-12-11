package uk.gov.cshr.vcm.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.Vacancy;

@Repository
public interface VacancyRepository extends CrudRepository<Vacancy, Long> {
}
