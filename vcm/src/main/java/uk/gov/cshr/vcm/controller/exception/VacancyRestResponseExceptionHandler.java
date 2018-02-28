package uk.gov.cshr.vcm.controller.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.cshr.vcm.controller.exception.VacancyException;

@ControllerAdvice
public class VacancyRestResponseExceptionHandler extends ResponseEntityExceptionHandler {

//    @ExceptionHandler({VacancyClosedException.class})
//    public ResponseEntity<Object> handleVacancyClosed(VacancyClosedException ex, WebRequest request) {
//        return handleExceptionInternal(ex, ex.getVacancyError(), new HttpHeaders(), ex.getVacancyError().getStatus(), request);
//    }

    @ExceptionHandler({VacancyException.class})
    public ResponseEntity<Object> handleLocationServiceException(VacancyException ex, WebRequest request) {

        return handleExceptionInternal(ex, ex.getVacancyError(), new HttpHeaders(), ex.getVacancyError().getStatus(), request);
    }
}
