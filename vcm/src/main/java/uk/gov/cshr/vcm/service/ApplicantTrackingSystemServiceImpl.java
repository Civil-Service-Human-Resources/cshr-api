package uk.gov.cshr.vcm.service;

import java.util.List;

import org.springframework.stereotype.Service;
import uk.gov.cshr.status.CSHRServiceStatus;
import uk.gov.cshr.status.StatusCode;
import uk.gov.cshr.vcm.controller.exception.InvalidApplicantTrackingSystemException;
import uk.gov.cshr.vcm.model.ApplicantTrackingSystemVendor;
import uk.gov.cshr.vcm.repository.ApplicantTrackingSystemVendorRepository;

@Service
public class ApplicantTrackingSystemServiceImpl implements ApplicantTrackingSystemService {
    private final ApplicantTrackingSystemVendorRepository applicantTrackingSystemVendorRepository;

    public ApplicantTrackingSystemServiceImpl(ApplicantTrackingSystemVendorRepository applicantTrackingSystemVendorRepository) {
        this.applicantTrackingSystemVendorRepository = applicantTrackingSystemVendorRepository;
    }

    @Override
    public void validateClientIdentifier(final String clientIdentifier) {
        List<ApplicantTrackingSystemVendor> vendor = applicantTrackingSystemVendorRepository.findByClientIdentifier(clientIdentifier);

        if (vendor.isEmpty()) {
            CSHRServiceStatus status = CSHRServiceStatus.builder()
                    .code(StatusCode.INTERNAL_SERVICE_ERROR.getCode())
                    .summary(clientIdentifier + " is not a recognised identifier for an Applicant Tracking System")
                    .build();

            throw new InvalidApplicantTrackingSystemException(status);
        }
    }
}
