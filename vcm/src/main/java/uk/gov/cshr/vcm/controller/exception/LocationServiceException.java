package uk.gov.cshr.vcm.controller.exception;

import org.springframework.http.HttpStatus;

public class LocationServiceException extends VacancyException {

    public static final String SERVICE_UNAVAILABLE_MESSAGE = "The location service is unavailable.";

    public LocationServiceException() {

        VacancyError vacancyError = VacancyError.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .message(SERVICE_UNAVAILABLE_MESSAGE).build();
        super.setVacancyError(vacancyError);
    }
}
