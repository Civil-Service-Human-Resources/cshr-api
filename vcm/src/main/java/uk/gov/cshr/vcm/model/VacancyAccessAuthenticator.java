package uk.gov.cshr.vcm.model;

/**
 * Defines the methods required by implementations of a vacancy access authenticator.
 *
 * Implementations ensure that a vacancy can only be seen by those who are allowed to view it.
 */
public interface VacancyAccessAuthenticator {
    VacancyAuthenticationStatus authenticate(Vacancy vacancy);
}
