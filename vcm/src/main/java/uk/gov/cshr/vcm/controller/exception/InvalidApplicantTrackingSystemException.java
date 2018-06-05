package uk.gov.cshr.vcm.controller.exception;

import uk.gov.cshr.exception.CSHRServiceException;
import uk.gov.cshr.status.CSHRServiceStatus;

/**
 * This exception is raised when an invalid applicant tracking system vendor was found when saving a vacancy
 */
public class InvalidApplicantTrackingSystemException extends CSHRServiceException {
    public InvalidApplicantTrackingSystemException(CSHRServiceStatus cshrServiceStatus) {
        super(cshrServiceStatus);
    }
}
