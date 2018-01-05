package uk.gov.cshr.vcm.controller;

import static org.mockito.BDDMockito.given;

import java.nio.charset.Charset;

import javax.inject.Inject;

import org.junit.After;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.vcm.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.fixture.CoordinatesFixture;
import uk.gov.cshr.vcm.repository.DepartmentRepository;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.LocationService;

public abstract class SearchTestConfiguration extends AbstractJUnit4SpringContextTests {
    static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    static final Coordinates BRISTOL = CoordinatesFixture.getInstance().getCoordinatesForBristol();
    static final Coordinates NEWCASTLE = CoordinatesFixture.getInstance().getCoordinatesForNewcastle();

    @Inject
    DepartmentRepository departmentRepository;
    @Inject
    VacancyRepository vacancyRepository;
    @Inject
    WebApplicationContext webApplicationContext;

    @MockBean
    LocationService locationService;

    MockMvc mockMvc;

    @After
    public void tearDown() {
        this.vacancyRepository.deleteAll();

        this.departmentRepository.deleteAll();
    }

    void initLocationService() throws LocationServiceException {
        given(locationService.find("bristol")).willReturn(BRISTOL);

        given(locationService.find("newcastle")).willReturn(NEWCASTLE);
    }
}
