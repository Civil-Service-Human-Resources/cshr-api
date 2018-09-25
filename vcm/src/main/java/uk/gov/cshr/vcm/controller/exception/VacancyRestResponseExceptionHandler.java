package uk.gov.cshr.vcm.controller.exception;

import java.io.IOException;

import javax.validation.ConstraintViolationException;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.cshr.status.CSHRServiceStatus;
import uk.gov.service.notify.NotificationClientException;

@ControllerAdvice
public class VacancyRestResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(VacancyRestResponseExceptionHandler.class);

    @ExceptionHandler({VacancyException.class})
    public ResponseEntity<Object> handleLocationServiceException(VacancyException ex, WebRequest request) {
        if (ex.getMessage() != null) {
            log.error(ex.getMessage(), ex);
        }
        return handleExceptionInternal(ex, ex.getVacancyError(), new HttpHeaders(), ex.getVacancyError().getStatus(), request);
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler({InvalidApplicantTrackingSystemException.class})
    @ResponseBody
    public CSHRServiceStatus handleInvalidAtsException(InvalidApplicantTrackingSystemException ex, WebRequest request) {
        log.error(ex.getCshrServiceStatus().getSummary(), ex);

        return ex.getCshrServiceStatus();
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        VacancyError vacancyError = new VacancyError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null, SearchStatusCode.EXCEPTION);
        return handleExceptionInternal(ex, vacancyError, new HttpHeaders(), vacancyError.getStatus(), request);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {

        log.error(ex.getMessage(), ex);
        VacancyError vacancyError = new VacancyError(HttpStatus.BAD_REQUEST, ex.getMessage(), null, SearchStatusCode.EXCEPTION);
        return handleExceptionInternal(ex, vacancyError, new HttpHeaders(), vacancyError.getStatus(), request);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedExceptionException(AccessDeniedException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        VacancyError vacancyError = new VacancyError(HttpStatus.FORBIDDEN, ex.getMessage(), null, SearchStatusCode.EXCEPTION);
        return handleExceptionInternal(ex, vacancyError, new HttpHeaders(), vacancyError.getStatus(), request);
    }

    @ExceptionHandler({IOException.class})
    public ResponseEntity<Object> handleIOException(IOException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        VacancyError vacancyError = new VacancyError(HttpStatus.BAD_REQUEST, ex.getMessage(), null, SearchStatusCode.EXCEPTION);
        return handleExceptionInternal(ex, vacancyError, new HttpHeaders(), vacancyError.getStatus(), request);
    }

    @ExceptionHandler({JwtException.class})
    public ResponseEntity<Object> handleExpiredJwtException(JwtException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        VacancyError vacancyError = new VacancyError(HttpStatus.UNAUTHORIZED, ex.getMessage(), null, SearchStatusCode.EXCEPTION);
        return handleExceptionInternal(ex, vacancyError, new HttpHeaders(), vacancyError.getStatus(), request);
    }

    @ExceptionHandler({NotificationClientException.class})
    public ResponseEntity<Object> handleNotificationClientException(NotificationClientException ex, WebRequest request) {

        log.error(ex.getMessage(), ex);
        HttpStatus httpStatus;

        try {
            httpStatus = HttpStatus.valueOf(ex.getHttpResult());
        } catch (IllegalArgumentException e) {
            httpStatus = HttpStatus.NOT_FOUND;
        }

        VacancyError vacancyError = new VacancyError(httpStatus, ex.getMessage(), null, SearchStatusCode.EXCEPTION);
        return handleExceptionInternal(ex, vacancyError, new HttpHeaders(), vacancyError.getStatus(), request);
    }
}
