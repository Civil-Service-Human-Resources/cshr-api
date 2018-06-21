package uk.gov.cshr.vcm.controller.exception;

public enum SearchStatusCode {

    INVALID_JWT,
    NULL_JWT,
    EXPIRED_JWT,
    JWT_NO_ELIGIBILITY_CLAIM,
    EXCEPTION
}
