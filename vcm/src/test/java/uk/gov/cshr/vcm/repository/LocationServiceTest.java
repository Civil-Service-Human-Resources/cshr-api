package uk.gov.cshr.vcm.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.cshr.vcm.service.LocationService;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class LocationServiceTest {
    private LocationService service;

    /*@Before
    public void setup() {
        service = new LocationService();
    }

    @After
    public void tearDown() {
        service = null;
    }

    @Test
    public void find_noResultsExpected_twoChars() {
        assertThat(service.find("bs"), is(nullValue()));
    }

    @Test
    public void find_noResultsExpected_withTooManyResults() {
        assertThat(service.find("Bri"), is(nullValue()));
    }

    @Test
    public void find_resultsExpected() {
        assertThat(service.find("bristol"), is(notNullValue()));
    }*/
}
