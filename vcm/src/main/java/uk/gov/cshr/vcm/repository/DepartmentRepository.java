package uk.gov.cshr.vcm.repository;

import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.Department;

@Repository
public interface DepartmentRepository extends PagingAndSortingRepository<Department, Long> {

    default Optional<Department> findById(Long id) {
        return Optional.ofNullable(this.findOne(id));
    }

    @Cacheable("departments")
    public Page<Department> findAllByOrderByNameAsc(Pageable pageable);
}
