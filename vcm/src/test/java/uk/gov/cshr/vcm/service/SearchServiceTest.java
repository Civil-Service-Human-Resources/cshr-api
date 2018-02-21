package uk.gov.cshr.vcm.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.model.fixture.CoordinatesFixture;
import uk.gov.cshr.vcm.model.fixture.VacancyFixture;
import uk.gov.cshr.vcm.repository.VacancyRepository;

/**
 * Tests {@link SearchService}
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VcmApplication.class)
@ContextConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class SearchServiceTest extends AbstractJUnit4SpringContextTests {
    private static final String BOON_DOCKS = "The boon docks";
    private static final String GOOGLE_API_SERVICE_DOWN = "Down";
    private static final String LAT_ONLY = "LatOnly";
    private static final String LONG_ONLY = "LongOnly";
    private static final String NEWCASTLE = "Newcastle";
    private static final Pageable PAGE_REQUEST = new PageRequest(0, 10);
    private static final String SWAINSWICK = "Swainswick";

    @Inject
    private SearchService searchService;

    @MockBean
    private LocationService locationService;
    @MockBean
    private VacancyRepository vacancyRepository;

    @Test(expected = LocationServiceException.class)
    public void find_externalLookupServiceHasException() throws LocationServiceException {
        given(locationService.find(GOOGLE_API_SERVICE_DOWN)).willThrow(LocationServiceException.class);
        searchService.search(buildVacancySearchParameters(GOOGLE_API_SERVICE_DOWN), null);
    }

    private VacancySearchParameters buildVacancySearchParameters(String place) {
        Location location = Location.builder().place(place).radius(30).build();

        return VacancySearchParameters.builder()
                .location(location)
                .build();
    }

    @Test
    public void find_nullCoordinatesReturned() throws LocationServiceException {
        given(locationService.find(NEWCASTLE)).willReturn(null);

        Page<Vacancy> actual = searchService.search(buildVacancySearchParameters( NEWCASTLE), PAGE_REQUEST);

        doNoResultsReturnedAsserts(actual);
    }

    private void doNoResultsReturnedAsserts(Page<Vacancy> actual) {
        assertThat(actual.getTotalElements(), Matchers.is(equalTo(0L)));
        assertThat(actual.getTotalPages(), Matchers.is(0));
        assertThat(actual.getNumberOfElements(), Matchers.is(0));
        assertThat(actual.getSize(), Matchers.is(10));
        assertThat(actual.getContent().isEmpty(), Matchers.is(true));
    }

    @Test
    public void find_noCoordinatesReturned() throws LocationServiceException {
        given(locationService.find(BOON_DOCKS)).willReturn(new Coordinates());

        Page<Vacancy> actual = searchService.search(buildVacancySearchParameters( BOON_DOCKS), PAGE_REQUEST);

        doNoResultsReturnedAsserts(actual);
    }

    @Test
    public void find_onlyLatitudeExists() throws LocationServiceException {
        Coordinates coordinates = CoordinatesFixture.getInstance().getCoordinatesForBristol();
        coordinates.setLongitude(null);

        given(locationService.find(LAT_ONLY)).willReturn(coordinates);

        Page<Vacancy> actual = searchService.search(buildVacancySearchParameters( LAT_ONLY), PAGE_REQUEST);

        doNoResultsReturnedAsserts(actual);
    }

    @Test
    public void find_onlyLongitudeExists() throws LocationServiceException {
        Coordinates coordinates = CoordinatesFixture.getInstance().getCoordinatesForBristol();
        coordinates.setLatitude(null);

        given(locationService.find(LONG_ONLY)).willReturn(coordinates);

        Page<Vacancy> actual = searchService.search(buildVacancySearchParameters( LONG_ONLY), PAGE_REQUEST);

        doNoResultsReturnedAsserts(actual);
    }

    @Test
    public void find_noJobsExist() throws LocationServiceException {
        given(locationService.find(SWAINSWICK)).willReturn(CoordinatesFixture.getInstance().getCoordinatesForBristol());
        given(vacancyRepository.search(any(), any())).willReturn(new PageImpl<>(new ArrayList<>(), PAGE_REQUEST, 0));

        Page<Vacancy> actual = searchService.search(buildVacancySearchParameters( SWAINSWICK), PAGE_REQUEST);

        doNoResultsReturnedAsserts(actual);
    }

    @Test
    public void find_AJobExists() throws LocationServiceException {
        List<Vacancy> vacancies = new ArrayList<>();
        vacancies.add(VacancyFixture.getInstance().getPrototype());
        given(locationService.find(SWAINSWICK)).willReturn(CoordinatesFixture.getInstance().getCoordinatesForBristol());
        given(vacancyRepository.search(any(), any())).willReturn(new PageImpl<>(vacancies, PAGE_REQUEST, 1));

        Page<Vacancy> actual = searchService.search(buildVacancySearchParameters( SWAINSWICK), PAGE_REQUEST);

        assertThat(actual.getTotalElements(), Matchers.is(equalTo(1L)));
        assertThat(actual.getTotalPages(), Matchers.is(1));
        assertThat(actual.getNumberOfElements(), Matchers.is(1));
        assertThat(actual.getSize(), Matchers.is(10));
        assertThat(actual.getContent().isEmpty(), Matchers.is(false));
        assertThat(actual.getContent(), hasSize(1));
    }
}
