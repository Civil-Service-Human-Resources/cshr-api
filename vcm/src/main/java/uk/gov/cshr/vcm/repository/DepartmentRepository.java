package uk.gov.cshr.vcm.repository;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.Department;

@Repository
public interface DepartmentRepository extends PagingAndSortingRepository<Department, Long> {

    final Logger log = LoggerFactory.getLogger(DepartmentRepository.class);

    default Optional<Department> findById(Long id) {
        return Optional.ofNullable(this.findOne(id));
    }

    @Cacheable(value = "departments")
    public Page<Department> findAllByOrderByNameAsc(Pageable pageable);

    @Override
    @CacheEvict(value = "departments", allEntries = true)
    public Department save(Department department);

    @Override
    @CacheEvict(value = "departments", allEntries = true)
    public void delete(Department department);

	@Override
	@Cacheable(value = "departments")
	public Iterable<Department> findAll();

    public Department findByIdentifier(String idendifier);

    public List<Department> findByParent(Department parent);
}
