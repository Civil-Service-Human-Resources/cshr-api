package uk.gov.cshr.vcm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
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
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.repository.DepartmentRepository;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.LocationService;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class SalaryRangeTest extends AbstractTestNGSpringContextTests {

    public static final double BRISTOL_LATITUDE = 51.4549291;
    public static final double BRISTOL_LONGITUDE = -2.6278111;

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

    @Before
    public void before() {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Department department = departmentRepository.save(Department.builder().name("Department One").build());
        createdDepartments.add(department);

        createVacancyWithSalaries(10000, 20000, department);
        createVacancyWithSalaries(20000, 30000, department);
        createVacancyWithSalaries(30000, 40000, department);
        createVacancyWithSalaries(40000, 50000, department);
        createVacancyWithSalaries(60000, 70000, department);

        given(locationService.find(any()))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE));
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
    public void testMaxSalary() throws Exception {

        Page<Vacancy> result = testExample(null, 69000);
        List<Vacancy> resultsList = result.getContent();

        List<Long> actualVacancyIDs
                = resultsList.stream()
                        .map(Vacancy::getId)
                        .collect(Collectors.toList());

        List<Long> expectedVacancyIDs
                = createdVacancies.stream()
                        .filter(v -> v.getSalaryMax() <= 69000)
                        .map(Vacancy::getId)
                        .collect(Collectors.toList());

        assertThat(actualVacancyIDs, is(equalTo(expectedVacancyIDs)));
    }

    @Test
    public void testMinSalary() throws Exception {

        Page<Vacancy> result = testExample(70000, null);
        List<Vacancy> resultsList = result.getContent();

        List<Long> actualVacancyIDs
                = resultsList.stream()
                        .map(Vacancy::getId)
                        .collect(Collectors.toList());

        List<Long> expectedVacancyIDs
                = createdVacancies.stream()
                        .filter(v -> v.getSalaryMax() >= 70000)
                        .map(Vacancy::getId)
                        .collect(Collectors.toList());

        assertThat(actualVacancyIDs, is(equalTo(expectedVacancyIDs)));
    }

    @Test
    public void testSalaryRange() throws Exception {

        Page<Vacancy> result = testExample(20001, 50000);
        List<Vacancy> resultsList = result.getContent();

        List<Long> actualVacancyIDs
                = resultsList.stream()
                        .map(Vacancy::getId)
                        .collect(Collectors.toList());

        List<Long> expectedVacancyIDs
                = createdVacancies.stream()
                        .filter(v -> v.getSalaryMax() >= 21000)
                        .filter(v -> v.getSalaryMax() <= 50000)
                        .map(Vacancy::getId)
                        .collect(Collectors.toList());

        assertThat(actualVacancyIDs, is(equalTo(expectedVacancyIDs)));
    }

    public Page<Vacancy> testExample(Integer minSalary, Integer maxSalary) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("SearchQueryDescription")
                .location(new Location("bristol", 30))
                .maxSalary(maxSalary)
                .minSalary(minSalary)
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

    private Vacancy getVacancyPrototype() {

        Vacancy vacancyPrototype = Vacancy.builder()
                .title("testTile1 SearchQueryTitle")
                .description("testDescription1 SearchQueryDescription")
                .location("testLocation1 SearchQueryLocation")
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
                .salaryMax(10)
                .numberVacancies(1)
                .latitude(BRISTOL_LATITUDE)
                .longitude(BRISTOL_LONGITUDE)
                .build();

        return vacancyPrototype;
    }

    private void createVacancyWithSalaries(int salaryMin, int salaryMax, Department department) {

        Vacancy vacancy = getVacancyPrototype();
        vacancy.setDepartment(department);
        vacancy.setSalaryMin(salaryMin);
        vacancy.setSalaryMax(salaryMax);
        vacancyRepository.save(vacancy);
        createdVacancies.add(vacancy);
    }
}
