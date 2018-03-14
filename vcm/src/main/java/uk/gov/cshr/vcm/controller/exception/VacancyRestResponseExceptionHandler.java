package uk.gov.cshr.vcm.controller.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class VacancyRestResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({VacancyException.class})
    public ResponseEntity<Object> handleLocationServiceException(VacancyException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getVacancyError(), new HttpHeaders(), ex.getVacancyError().getStatus(), request);
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        ex.printStackTrace();
        VacancyError vacancyError = new VacancyError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
        return handleExceptionInternal(ex, vacancyError, new HttpHeaders(), vacancyError.getStatus(), request);
    }
}
