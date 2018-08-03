package uk.gov.cshr.vcm.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.EmailExtension;

@Repository
public interface EmailExtensionRepository extends PagingAndSortingRepository<EmailExtension, Long> {


}
