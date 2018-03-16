package uk.gov.cshr.vcm.repository;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.vcm.VcmApplication;
import static uk.gov.cshr.vcm.controller.SalaryRangeTest.NEWCASTLE_LATITUDE;
import static uk.gov.cshr.vcm.controller.SalaryRangeTest.NEWCASTLE_LONGITUDE;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyLocation;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.service.HibernateSearchService;
import uk.gov.cshr.vcm.service.LocationService;
import uk.gov.cshr.vcm.service.SearchService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class HibernateSearchTest extends AbstractTestNGSpringContextTests {

    public static final double BRISTOL_LATITUDE = 51.4549291;
    public static final double BRISTOL_LONGITUDE = -2.6278111;

    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30);
    private static final Timestamp ONE_DAY_AGO = getTime(-1);

    @Autowired
    private VacancyRepository vacancyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Inject
    private HibernateSearchService hibernateSearchService;
    @Inject
    private SearchService searchService;

    @MockBean
    private LocationService locationService;

    private final List<Vacancy> createdVacancies = new ArrayList<>();
    private final List<Department> createdDepartments = new ArrayList<>();

    private Department department;

    private MockMvc mockMvc;

    @Inject
    private WebApplicationContext webApplicationContext;

    @Before
    public void before() throws LocationServiceException {

        vacancyRepository.deleteAll();
        departmentRepository.deleteAll();

        department = departmentRepository.save(Department.builder().name("Department One").build());
        createdDepartments.add(department);

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        department = departmentRepository.save(Department.builder().name("Department One").build());
        createdDepartments.add(department);

        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        given(locationService.find("newcastle"))
                .willReturn(new Coordinates(NEWCASTLE_LONGITUDE, NEWCASTLE_LATITUDE, "North East"));
    }

    @After
    public void after() {

//        for (Vacancy createdVacancy : createdVacancies) {
//            vacancyRepository.delete(createdVacancy);
//        }
//
//        for (Department createdDepartment : createdDepartments) {
//            departmentRepository.delete(createdDepartment);
//        }
    }

    @Test
    public void testSearchVacancy() throws LocationServiceException, IOException {

        Vacancy vacancy = getVacancyPrototype();
        vacancy.setTitle("Engineer");
        vacancy.setRegions("Scotland, South East");
        vacancy.setSalaryMax(10001);
        vacancy.setIdentifier(System.currentTimeMillis());
        saveVacancy(vacancy);

        vacancy = getVacancyPrototype();
        vacancy.setTitle("baker");
        vacancy.setRegions("South West, North East");
        vacancy.setSalaryMax(10001);
        vacancy.setIdentifier(System.currentTimeMillis());
        saveVacancy(vacancy);

        vacancy = getVacancyPrototype();
        vacancy.setTitle("candle maker");
        vacancy.setRegions("West Midlands, West Scotland");
        vacancy.setSalaryMax(10001);
        vacancy.setIdentifier(System.currentTimeMillis());
        saveVacancy(vacancy);

        // closed: will not be returned
        vacancy = getVacancyPrototype();
        vacancy.setTitle("candle maker");
        vacancy.setRegions("South West");
        vacancy.setSalaryMax(10001);
        vacancy.setClosingDate(ONE_DAY_AGO);
        vacancy.setIdentifier(System.currentTimeMillis());
        saveVacancy(vacancy);

        // not open yet: will not be returned
        vacancy = getVacancyPrototype();
        vacancy.setTitle("candle maker");
        vacancy.setRegions("South West");
        vacancy.setSalaryMax(10001);
        vacancy.setPublicOpeningDate(THIRTY_DAYS_FROM_NOW);
        vacancy.setClosingDate(THIRTY_DAYS_FROM_NOW);
        vacancy.setIdentifier(System.currentTimeMillis());
        saveVacancy(vacancy);

        // max salary 500, will not be returned when min salary is > 500
        vacancy = getVacancyPrototype();
        vacancy.setTitle("candle maker");
        vacancy.setRegions("South West");
        vacancy.setSalaryMax(500);
        vacancy.setPublicOpeningDate(ONE_DAY_AGO);
        vacancy.setClosingDate(THIRTY_DAYS_FROM_NOW);
        vacancy.setIdentifier(System.currentTimeMillis());
        saveVacancy(vacancy);

        // min salary 10000, will not be returned when max salary is > 9999
        vacancy = getVacancyPrototype();
        vacancy.setTitle("candle maker");
        vacancy.setRegions("South West");
        vacancy.setSalaryMin(10000);
        vacancy.setPublicOpeningDate(ONE_DAY_AGO);
        vacancy.setClosingDate(THIRTY_DAYS_FROM_NOW);
        vacancy.setIdentifier(System.currentTimeMillis());
        saveVacancy(vacancy);

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .location(Location.builder().place("Bristol").radius(30).build())
                .minSalary(501)
                .keyword("engineer")
                .build();

        Coordinates coordinates = new Coordinates();
        coordinates.setLatitude(BRISTOL_LATITUDE);
        coordinates.setLongitude(BRISTOL_LONGITUDE);
        coordinates.setRegion("South West");
        SearchParameters searchParameters = SearchParameters.builder()
                .vacancySearchParameters(vacancySearchParameters)
                .coordinates(coordinates)
                .build();

        Page<VacancyLocation> results = hibernateSearchService.search(searchParameters, null);
        Assert.assertEquals(3, results.getContent().size());
//        Assert.assertEquals(vacancy.getId(), results.getContent().get(0).getId());
    }

    private Vacancy getVacancyPrototype() {

        Vacancy vacancyPrototype = Vacancy.builder()
                .title("testTile1 SearchQueryTitle")
            .description("testDescription1 SearchQueryDescription")
                .grade("testGrade1 SearchQueryGrade")
                .responsibilities("testResponsibilities1")
            .workingHours("testWorkingHours1")
            .closingDate(THIRTY_DAYS_FROM_NOW)
            .publicOpeningDate(ONE_DAY_AGO)
            .contactName("testContactName1")
            .contactDepartment("testContactDepartment1")
            .contactEmail("testContactEmail1")
            .contactTelephone("testContactTelephone1")
            .eligibility("testEligibility1")
            .salaryMin(0)
            .identifier(1L)
                .numberVacancies(1)
                .build();

        return vacancyPrototype;

}

    private Vacancy saveVacancy(Vacancy vacancy) {

        vacancy.setDepartment(department);
        vacancyRepository.save(vacancy);
        createdVacancies.add(vacancy);
        return vacancy;
    }


    private static Timestamp getTime(int numberOfDaysFromNow) {
        Date date = Date.from(LocalDateTime.now().plusDays(numberOfDaysFromNow).atZone(ZoneId.systemDefault()).toInstant());
        return new Timestamp(date.getTime());
    }
}
