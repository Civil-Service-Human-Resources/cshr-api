package uk.gov.cshr.vcm.controller.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.controller.VacancyPage;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyLocation;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.repository.DepartmentRepository;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.HibernateSearchService;
import uk.gov.cshr.vcm.service.LocationService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class OverseasJobsTest extends AbstractTestNGSpringContextTests {

    public static final double BRISTOL_LATITUDE = 51.4549291;
    public static final double BRISTOL_LONGITUDE = -2.6278111;

    public static final double NEWCASTLE_LATITUDE = 54.9806308;
    public static final double NEWCASTLE_LONGITUDE = -1.6167437;

    public static final Coordinates BRISTOL_COORDINATES = new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West");
    public static final Coordinates NEWCASTLE_COORDINATES = new Coordinates(NEWCASTLE_LONGITUDE, NEWCASTLE_LATITUDE, "North East");
    private static final String BRISTOL_JOB_TITLE = "bristol job";
    private static final String NEWCASTLE_JOB_TITLE = "newcastle job";

    VacancyLocation bristolLocation = VacancyLocation.builder()
            .latitude(BRISTOL_LATITUDE)
            .longitude(BRISTOL_LONGITUDE)
            .location("bristol")
            .build();

    VacancyLocation newcastleLocation = VacancyLocation.builder()
            .latitude(NEWCASTLE_LATITUDE)
            .longitude(NEWCASTLE_LONGITUDE)
            .location("newcastle")
            .build();

    final private MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    @Inject
    private WebApplicationContext webApplicationContext;

    @Inject
    private VacancyRepository vacancyRepository;

    @Inject
    private DepartmentRepository departmentRepository;

    @Inject
    private HibernateSearchService hibernateSearchService;

    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30);
    private static final Timestamp ONE_DAY_AGO = getTime(-1);

    private Department department;

    @Before
    public void before() throws LocationServiceException {

        hibernateSearchService.purge();
        vacancyRepository.deleteAll();
        departmentRepository.deleteAll();

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        department = departmentRepository.save(Department.builder().name("Department One").build());

        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        given(locationService.find("newcastle"))
                .willReturn(new Coordinates(NEWCASTLE_LONGITUDE, NEWCASTLE_LATITUDE, "North East"));
    }

    @After
    public void after() {
    }

    @Test
    public void testOverseasJobs() throws Exception {

        boolean INCLUDE_OVERSEAS = true;
        boolean DONT_INCLUDE_OVERSEAS = false;

        // As we're searching Bristol, this should always come up
        Vacancy localVacancy = getVacancyPrototype(BRISTOL_JOB_TITLE, bristolLocation);
        localVacancy.setOverseasJob(Boolean.FALSE);
        vacancyRepository.save(localVacancy);

        // this should only come up when overseas is selected
        Vacancy overseasVacancy = getVacancyPrototype(NEWCASTLE_JOB_TITLE, newcastleLocation);
        overseasVacancy.setOverseasJob(Boolean.TRUE);
        vacancyRepository.save(overseasVacancy);

        Page<Vacancy> result = findVancancies("bristol", DONT_INCLUDE_OVERSEAS);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertEquals(1, resultsList.size());
        Assert.assertTrue("Expect Bristol Job", resultsList.get(0).getTitle().equals(BRISTOL_JOB_TITLE));

        result = findVancancies("bristol", INCLUDE_OVERSEAS);
        resultsList = result.getContent();

        Assert.assertEquals(2, resultsList.size());
    }

    public Page<Vacancy> findVancancies(String place, Boolean includeOverseas) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("SearchQueryDescription")
                .location(new Location(place, 30))
                .overseasJob(includeOverseas)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(vacancySearchParameters);

        MvcResult mvcResult = this.mockMvc.perform(post("/vacancy/search")
				.with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        String searchResponse = mvcResult.getResponse().getContentAsString();
        return objectMapper.readValue(searchResponse, VacancyPage.class);
    }

    private static Timestamp getTime(int numberOfDaysFromNow) {
        Date date = Date.from(LocalDateTime.now().plusDays(numberOfDaysFromNow).atZone(ZoneId.systemDefault()).toInstant());
        return new Timestamp(date.getTime());
    }

    private Vacancy getVacancyPrototype(String jobTitle, VacancyLocation vacancyLocation) {

        Vacancy vacancyPrototype = Vacancy.builder()
                .title(jobTitle)
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
                .salaryMax(Integer.MAX_VALUE)
                .identifier(1L)
                .numberVacancies(1)
                .identifier(System.currentTimeMillis())
                .build();

        vacancyPrototype.getVacancyLocations().add(vacancyLocation);
        vacancyLocation.setVacancy(vacancyPrototype);

        return vacancyPrototype;
    }
}
