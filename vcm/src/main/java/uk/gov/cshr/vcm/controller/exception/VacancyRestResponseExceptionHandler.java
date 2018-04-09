package uk.gov.cshr.vcm.controller.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.cshr.error.CSHRServiceStatus;

@ControllerAdvice
public class VacancyRestResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(VacancyRestResponseExceptionHandler.class);

    @ExceptionHandler({VacancyException.class})
    public ResponseEntity<Object> handleLocationServiceException(VacancyException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return handleExceptionInternal(ex, ex.getVacancyError(), new HttpHeaders(), ex.getVacancyError().getStatus(), request);
    }

    @ExceptionHandler({InvalidApplicantTrackingSystemException.class})
    @ResponseBody
    public CSHRServiceStatus handleInvalidAtsException(InvalidApplicantTrackingSystemException ex, WebRequest request) {
        log.error(ex.getCshrServiceStatus().getSummary(), ex);

        return ex.getCshrServiceStatus();
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        VacancyError vacancyError = new VacancyError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
        return handleExceptionInternal(ex, vacancyError, new HttpHeaders(), vacancyError.getStatus(), request);
    }
}
