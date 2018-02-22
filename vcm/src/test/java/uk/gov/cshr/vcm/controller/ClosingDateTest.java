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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.fail;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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
import uk.gov.cshr.vcm.controller.exception.VacancyClosedException;
import uk.gov.cshr.vcm.controller.exception.VacancyError;
import uk.gov.cshr.vcm.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.repository.DepartmentRepository;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.LocationService;

@Ignore
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class ClosingDateTest extends AbstractTestNGSpringContextTests {

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

    private static final Timestamp YESTERDAY = getTime(-1);
    private static final Timestamp TODAY = getTime(0);
    private static final Timestamp TOMORROW = getTime(+1);
    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30);

    private final List<Vacancy> createdVacancies = new ArrayList<>();
    private final List<Department> createdDepartments = new ArrayList<>();

    private Department department;

    @Before
    public void before() throws LocationServiceException {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        department = departmentRepository.save(
                Department.builder()
                        .name("Department One")
                        .disabilityLogo("disabilityLogo")
                        .build());

        createdDepartments.add(department);

        given(locationService.find(any()))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));
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
    public void testExcludeClosedVacancies() throws Exception {

        createVacancyWithClosingDate(YESTERDAY, department);

        // beacuse these are timestamp basesd this job will be closed
        createVacancyWithClosingDate(TODAY, department);
        createVacancyWithClosingDate(TOMORROW, department);
        createVacancyWithClosingDate(THIRTY_DAYS_FROM_NOW, department);

        Page<Vacancy> result = findVancancies();
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());

        resultsList.stream().filter((vacancy) -> (vacancy.getClosingDate().compareTo(new Date()) == -1))
                .forEachOrdered((vacancy) -> {
                    fail("vacancy.getClosingDate() in past: " + vacancy.getClosingDate());
                });
    }

    @Test
    public void testFindRegionalVacancies() throws Exception {

        createVacancyWithRegions(department, "South West, Scotland");

        Page<Vacancy> result = findVancancies();
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());

        for (Vacancy vacancy : resultsList) {

        }
    }

    @Test
    public void testGetClosedVacancy() throws Exception {

        Vacancy closedVacancy = createVacancyWithClosingDate(YESTERDAY, department);

        MvcResult mvcResult = this.mockMvc.perform(get("/vacancy/" + closedVacancy.getId())
                .contentType(APPLICATION_JSON_UTF8)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isGone())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        VacancyError vacancyError = objectMapper.readValue(response, VacancyError.class);

        assertThat(vacancyError.getStatus(), is(HttpStatus.GONE));
        assertThat(vacancyError.getMessage(),
                containsString(VacancyClosedException.CLOSED_MESSAGE));
    }

    @Test
    public void testUpdateClosedVacancy() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        Vacancy closedVacancy = createVacancyWithClosingDate(YESTERDAY, department);
        closedVacancy.setClosingDate(THIRTY_DAYS_FROM_NOW);

        MvcResult mvcUpdateResult = this.mockMvc.perform(put("/vacancy/" + closedVacancy.getId())
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(closedVacancy))
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcFindResult = this.mockMvc.perform(get("/vacancy/" + closedVacancy.getId())
                .contentType(APPLICATION_JSON_UTF8)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        String updatedVacancyJson = mvcFindResult.getResponse().getContentAsString();
        Vacancy vacancy = objectMapper.readValue(updatedVacancyJson, Vacancy.class);

        Assert.assertEquals("Date updated", THIRTY_DAYS_FROM_NOW, vacancy.getClosingDate());
        Assert.assertEquals("dept disability logo",
                vacancy.getDepartment().getDisabilityLogo(),
                department.getDisabilityLogo());
    }

    public Page<Vacancy> findVancancies() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("SearchQueryDescription")
                .location(new Location("bristol", 30))
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

    private Vacancy createVacancyWithRegions(Department department, String regions) {

        Vacancy vacancy = createVacancyPrototype();
        vacancy.setRegions(regions);
        vacancy.setLatitude(null);
        vacancy.setLongitude(null);
        vacancy.setDepartment(department);
        vacancyRepository.save(vacancy);
        createdVacancies.add(vacancy);
        return vacancy;
    }

    private Vacancy createVacancyWithClosingDate(Timestamp closingDate, Department department) {

        Vacancy vacancy = createVacancyPrototype();

        vacancy.setDepartment(department);
        vacancy.setClosingDate(closingDate);
        vacancyRepository.save(vacancy);
        createdVacancies.add(vacancy);
        return vacancy;
    }

    private Vacancy createVacancyPrototype() {

        return Vacancy.builder()
                .title("testTile1 SearchQueryTitle")
                .description("testDescription1 SearchQueryDescription")
                .location("testLocation1 SearchQueryLocation")
                .grade("testGrade1 SearchQueryGrade")
                .responsibilities("testResponsibilities1")
                .workingHours("testWorkingHours1")
                .closingDate(THIRTY_DAYS_FROM_NOW)
                .publicOpeningDate(YESTERDAY)
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
                .identifier(1L)
                .role("role")
                .build();
    }
}
