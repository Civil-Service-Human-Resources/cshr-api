package uk.gov.cshr.vcm.model;

import static org.junit.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;
import uk.gov.cshr.vcm.model.fixture.VacancyFixture;

/**
 * Tests {@link ApplyURLSanitiser}
 */
public class ApplyURLSanitiserTest {
    private static final String APPLY_URL_SHOULD_HAVE_BEEN_NULL = "The apply url should have been null";
    private static final String HTTP_WWW_FOO_NET = "http://www.foo.net";
    private static final String HTTPS_WWW_FOO_NET = "https://www.foo.net";

    @Test
    public void sanitise_invalidScheme() {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        vacancy.setApplyURL("grr://www.foo.net");

        assertNull("The apply url should have been null", ApplyURLSanitiser.sanitise(vacancy).getApplyURL());
    }

    @Test
    public void sanitise_noSchemeSupplied() {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        vacancy.setApplyURL("www.foo.net");

        assertEquals(APPLY_URL_SHOULD_HAVE_BEEN_NULL, HTTPS_WWW_FOO_NET,
                ApplyURLSanitiser.sanitise(vacancy).getApplyURL());
    }

    @Test
    public void sanitise_httpSchemeSupplied() {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        vacancy.setApplyURL(HTTP_WWW_FOO_NET);

        assertEquals("The apply url should have been null", HTTP_WWW_FOO_NET,
                ApplyURLSanitiser.sanitise(vacancy).getApplyURL());
    }

    @Test
    public void sanitise_httpsSchemeSupplied() {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        vacancy.setApplyURL(HTTPS_WWW_FOO_NET);

        assertEquals("The apply url should have been null", HTTPS_WWW_FOO_NET,
                ApplyURLSanitiser.sanitise(vacancy).getApplyURL());
    }
}
