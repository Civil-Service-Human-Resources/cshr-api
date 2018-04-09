package uk.gov.cshr.vcm.repository;

import java.util.Optional;

import org.springframework.data.repository.query.Param;
import uk.gov.cshr.vcm.model.ApplicantTrackingSystemVendor;

public interface ApplicationTrackingSystemVendorRepositoryCustom {
    /**
     * Finds instances of ApplicantTrackingSystemVendor using clientIdentifier as the search criteria.
     *
     * @param clientIdentifier search criteria to find matching instances of ApplicantTrackingSystemVendor
     * @return a Optional Vacancy if found or empty
     */
    Optional<ApplicantTrackingSystemVendor> findByClientIdentifier(@Param("clientIdentifier") String clientIdentifier);
}
