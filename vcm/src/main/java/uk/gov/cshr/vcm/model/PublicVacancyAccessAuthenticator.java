package uk.gov.cshr.vcm.model;

import java.util.Date;

/**
 * This class is responsible for authenticating access to a vacancy by a member of the public.
 */
public class PublicVacancyAccessAuthenticator implements VacancyAccessAuthenticator {
    public VacancyAuthenticationStatus authenticate(Vacancy vacancy) {
        VacancyAuthenticationStatus status;
        Date now = new Date();

        if (vacancy.getPublicOpeningDate() == null) {
            status = VacancyAuthenticationStatus.NOT_AUTHENTICATED;
        } else if (!vacancy.getActive() || vacancy.getClosingDate().before(now)) {
            status = VacancyAuthenticationStatus.CLOSED;
        } else if (vacancy.getPublicOpeningDate().after(now)) {
            status = VacancyAuthenticationStatus.NOT_OPEN;
        } else {
            status = VacancyAuthenticationStatus.AUTHENTICATED;
        }

        return status;
    }
}
