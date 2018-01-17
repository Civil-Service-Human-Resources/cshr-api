package uk.gov.cshr.vcm.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.Department;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends PagingAndSortingRepository<Department, Long> {

    default Optional<Department> findById(Long id) {
        return Optional.ofNullable(this.findOne(id));
    }
}
