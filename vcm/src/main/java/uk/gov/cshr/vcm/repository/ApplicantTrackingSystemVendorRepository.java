package uk.gov.cshr.vcm.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.vcm.model.ApplicantTrackingSystemVendor;

/**
 * Specifies the methods available for working with instances of ApplicantTrackingSystemVendor
 */
@Repository
@Transactional
public interface ApplicantTrackingSystemVendorRepository extends CrudRepository<ApplicantTrackingSystemVendor, Long> {
    /**
     * Finds instances of ApplicantTrackingSystemVendor using clientIdentifier as the search criteria.
     *
     * @param clientIdentifier search criteria to find matching instances of ApplicantTrackingSystemVendor
     * @return a list of ApplicantTrackingSystemVendor instances that match the given search criteria
     */
    @Query("select atsv from ApplicantTrackingSystemVendor atsv where atsv.clientIdentifier = :clientIdentifier")
    List<ApplicantTrackingSystemVendor> findByClientIdentifier(@Param("clientIdentifier") String clientIdentifier);
}
