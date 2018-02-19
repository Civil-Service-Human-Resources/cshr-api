package uk.gov.cshr.vcm.controller.exception;

import org.springframework.http.HttpStatus;

public class VacancyClosedException extends VacancyException {

    public static final String CLOSED_MESSAGE = "Vacancy closed";

    public VacancyClosedException() {
        VacancyError vacancyError = VacancyError.builder()
                .status(HttpStatus.GONE)
                .message(CLOSED_MESSAGE).build();
        super.setVacancyError(vacancyError);
    }
}
