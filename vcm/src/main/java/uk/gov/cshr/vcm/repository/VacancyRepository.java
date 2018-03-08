package uk.gov.cshr.vcm.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.Vacancy;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface VacancyRepository extends PagingAndSortingRepository<Vacancy, Long>, VacancyRepositoryCustom {

    default Optional<Vacancy> findById(Long id) {

        return Optional.ofNullable(this.findOne(id));
    }

    @Override
    public <S extends Vacancy> S save(S entity);
}
