package uk.gov.cshr.vcm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
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
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.repository.DepartmentRepository;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.LocationService;

//@Ignore
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

    public static final Coordinates BRISTOL = new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West");
    public static final Coordinates NEWCASTLE = new Coordinates(NEWCASTLE_LONGITUDE, NEWCASTLE_LATITUDE, "North East");

    final private MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    @Inject
    private WebApplicationContext webApplicationContext;

    @Inject
    private VacancyRepository vacancyRepository;

    @Inject
    private DepartmentRepository departmentRepository;

    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30);
    private static final Timestamp ONE_DAY_AGO = getTime(-1);

    private final List<Vacancy> createdVacancies = new ArrayList<>();
    private final List<Department> createdDepartments = new ArrayList<>();

    private Department department;

    @Before
    public void before() throws LocationServiceException {

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

        for (Vacancy createdVacancy : createdVacancies) {
            vacancyRepository.delete(createdVacancy);
        }

        for (Department createdDepartment : createdDepartments) {
            departmentRepository.delete(createdDepartment);
        }
    }

    @Test
    public void testOverseasJobs() throws Exception {

        boolean INCLUDE_OVERSEAS = true;
        boolean DONT_INCLUDE_OVERSEAS = false;

        // As we're searching Bristol, this should always come up
        Vacancy localVacancy = getVacancyPrototype(BRISTOL);
        localVacancy.setOverseasJob(Boolean.FALSE);
        vacancyRepository.save(localVacancy);
        createdVacancies.add(localVacancy);

        // this should only come up when overseas is selected
        Vacancy overseasVacancy = getVacancyPrototype(NEWCASTLE);
        overseasVacancy.setOverseasJob(Boolean.TRUE);
        vacancyRepository.save(overseasVacancy);
        createdVacancies.add(overseasVacancy);

        Page<Vacancy> result = findVancancies("bristol", DONT_INCLUDE_OVERSEAS);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertEquals(1, resultsList.size());

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

    private Vacancy getVacancyPrototype(Coordinates coordinates) {

        Vacancy vacancyPrototype = Vacancy.builder()
                .title("testTile1 SearchQueryTitle")
                .description("testDescription1 SearchQueryDescription")
                //                .location("testLocation1 SearchQueryLocation")
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
                //                .latitude(coordinates.getLatitude())
                //                .longitude(coordinates.getLongitude())
                .identifier(System.currentTimeMillis())
                .build();

        return vacancyPrototype;
    }

    private Vacancy createVacancyWithSalaryRange(Integer salaryMin, Integer salaryMax, Department department, Coordinates coordinates) {

        Vacancy vacancy = getVacancyPrototype(coordinates);
        vacancy.setDepartment(department);
        vacancy.setSalaryMin(salaryMin);
        vacancy.setSalaryMax(salaryMax);
        vacancyRepository.save(vacancy);
        createdVacancies.add(vacancy);
        return vacancy;
    }
}
