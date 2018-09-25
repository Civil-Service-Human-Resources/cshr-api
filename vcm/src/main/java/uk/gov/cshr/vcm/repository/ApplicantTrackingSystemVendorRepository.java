package uk.gov.cshr.vcm.repository;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.ApplicantTrackingSystemVendor;

/**
 * Specifies the methods available for working with instances of ApplicantTrackingSystemVendor
 */
@Repository
@Transactional
public interface ApplicantTrackingSystemVendorRepository extends CrudRepository<ApplicantTrackingSystemVendor, Long>, ApplicationTrackingSystemVendorRepositoryCustom {
}
