package uk.gov.cshr.vcm.controller.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
public class VacancyClosedException extends VacancyException {

    public static final String CLOSED_MESSAGE = "Vacancy closed";

    public VacancyClosedException(Long vacancyID) {
        VacancyError vacancyError = VacancyError.builder()
                .status(HttpStatus.GONE)
                .message(CLOSED_MESSAGE).build();
        super.setVacancyError(vacancyError);
    }
}
