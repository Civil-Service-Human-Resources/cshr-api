package uk.gov.cshr.vcm.service;

/**
 * Specifies the methods for working with Application Tracking System (ATS) vendors.
 */
public interface ApplicantTrackingSystemService {
    /**
     * This method is responsible for validating if an instance of an ATS can be found for the given clientIdentifier
     * <p>
     * A match must be found to be able to work with Application Tracking System vendor
     * <p>
     * The method will throw {@link uk.gov.cshr.vcm.controller.exception.InvalidApplicantTrackingSystemException} if
     * no vendor can be found for the given clientIdentifier.
     *
     * @param clientIdentifier identifier of a ATS vendor to validate
     */
    void validateClientIdentifier(String clientIdentifier);
}
