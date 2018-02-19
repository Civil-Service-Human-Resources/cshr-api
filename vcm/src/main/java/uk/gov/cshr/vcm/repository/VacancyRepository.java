package uk.gov.cshr.vcm.repository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.Vacancy;

@Repository
@Transactional
public interface VacancyRepository extends PagingAndSortingRepository<Vacancy, Long>, VacancyRepositoryCustom {

    default Optional<Vacancy> findById(Long id) {

        return Optional.ofNullable(this.findOne(id));
    }
}
