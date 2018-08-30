package uk.gov.cshr.vcm.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.cshr.vcm.util.TestUtils.getTime;

import org.junit.Test;
import uk.gov.cshr.vcm.model.fixture.VacancyFixture;

/**
 * Tests {@link PublicVacancyAccessAuthenticator}
 */
public class PublicVacancyAccessAuthenticatorTest {
    @Test
    public void authenticate_vacancyNotActive() {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        vacancy.setPublicOpeningDate(getTime(-10));
        vacancy.setActive(false);

        assertThat(new PublicVacancyAccessAuthenticator().authenticate(vacancy), is(equalTo(VacancyAuthenticationStatus.CLOSED)));
    }

    @Test
    public void authenticate_closingDatePassed() {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        vacancy.setPublicOpeningDate(getTime(-10));
        vacancy.setActive(true);
        vacancy.setClosingDate(getTime(-1));

        assertThat(new PublicVacancyAccessAuthenticator().authenticate(vacancy), is(equalTo(VacancyAuthenticationStatus.CLOSED)));
    }

    @Test
    public void authenticate_publicOpeningDateNotReached() {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        vacancy.setActive(true);
        vacancy.setClosingDate(getTime(10));
        vacancy.setPublicOpeningDate(getTime(1));

        assertThat(new PublicVacancyAccessAuthenticator().authenticate(vacancy), is(equalTo(VacancyAuthenticationStatus.NOT_OPEN)));
    }

    @Test
    public void authenticate_noPublicOpeningDate() {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        vacancy.setActive(true);
        vacancy.setClosingDate(getTime(10));
        vacancy.setPublicOpeningDate(null);

        assertThat(new PublicVacancyAccessAuthenticator().authenticate(vacancy), is(equalTo(VacancyAuthenticationStatus.NOT_AUTHENTICATED)));
    }

    @Test
    public void authenticate_vacancyOpenToPublic() {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        vacancy.setActive(true);
        vacancy.setClosingDate(getTime(10));
        vacancy.setPublicOpeningDate(getTime(-10));

        assertThat(new PublicVacancyAccessAuthenticator().authenticate(vacancy), is(equalTo(VacancyAuthenticationStatus.AUTHENTICATED)));
    }
}
