package uk.gov.cshr.vcm.controller.search;

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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import org.mockito.Mockito;
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
import uk.gov.cshr.vcm.controller.VacancyPage;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.controller.exception.VacancyClosedException;
import uk.gov.cshr.vcm.controller.exception.VacancyError;
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

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class VacancySearchTests extends AbstractTestNGSpringContextTests {

    public static final double BRISTOL_LATITUDE = 51.4549291;
    public static final double BRISTOL_LONGITUDE = -2.6278111;

    public static final double NEWCASTLE_LATITUDE = 54.9806308;
    public static final double NEWCASTLE_LONGITUDE = -1.6167437;

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

    @Inject
    private HibernateSearchService hibernateSearchService;

    private static final Timestamp YESTERDAY = getTime(-1);
    private static final Timestamp TODAY = getTime(0);
    private static final Timestamp TOMORROW = getTime(+1);
    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30);

    private Department department1;
    private Department department2;

    private final VacancyLocation bristolLocation1 = VacancyLocation.builder()
            .latitude(BRISTOL_LATITUDE)
            .longitude(BRISTOL_LONGITUDE)
            .location("Bristol1")
            .build();

    private final VacancyLocation bristolLocation2 = VacancyLocation.builder()
            .latitude(BRISTOL_LATITUDE)
            .longitude(BRISTOL_LONGITUDE)
            .location("Bristol2")
            .build();

    private final VacancyLocation newcastleLocation = VacancyLocation.builder()
            .latitude(NEWCASTLE_LATITUDE)
            .longitude(NEWCASTLE_LONGITUDE)
            .location("Newcastle")
            .build();

    @Before
    public void before() throws LocationServiceException {

        hibernateSearchService.purge();
        vacancyRepository.deleteAll();
        departmentRepository.deleteAll();;

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        department1 = departmentRepository.save(
                Department.builder()
                        .name("Department One")
                        .disabilityLogo("disabilityLogo")
                        .build());

        department2 = departmentRepository.save(
                Department.builder()
                        .name("Department two")
                        .disabilityLogo("disabilityLogo")
                        .build());
    }

    @After
    public void after() {
    }

    @Test
    public void testExcludeClosedVacancies() throws Exception {

        given(locationService.find(any()))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        createVacancyWithClosingDate("yesterday", YESTERDAY, department1);

        // beacuse these are timestamp basesd this job will be closed
        Timestamp earlierToday = new Timestamp(TODAY.getTime() - 10000);
        createVacancyWithClosingDate("earlier today", earlierToday, department1);

        createVacancyWithClosingDate("tomorrow", TOMORROW, department1);
        createVacancyWithClosingDate("thirty days time", THIRTY_DAYS_FROM_NOW, department1);

        Page<Vacancy> result = findVancanciesInPlace("bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());

        Date now = new Date();

        for (Vacancy vacancy : resultsList) {
            if (vacancy.getClosingDate().before(now)) {
                fail("vacancy.getClosingDate() in past: vacancy="
                        + vacancy.getTitle()
                        + " closingDate="
                        + vacancy.getClosingDate()
                        + " now=" + now);
            }
        }
    }

    @Test
    public void testFindKeywordInTitle() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
        newcastleVacancy.setTitle("Newcastle Job");
        saveVacancy(newcastleVacancy);

        Page<Vacancy> result = findVancanciesByKeyword("newcastle");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertEquals("Newcastle Job", resultsList.get(0).getTitle());

        Vacancy bristolVacancy = createVacancyPrototype(bristolLocation1);
        bristolVacancy.setTitle("Bristol Job");
        saveVacancy(bristolVacancy);

        result = findVancanciesByKeyword("newcastle");
        resultsList = result.getContent();

        Assert.assertEquals("Newcastle Job", resultsList.get(0).getTitle());
        Assert.assertEquals("1", 1, resultsList.size());
    }

    @Test
    public void testFindMultipleLocations() throws Exception {

        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
        saveVacancy(newcastleVacancy);

        Page<Vacancy> result = findVancanciesInPlace("bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected no results", resultsList.isEmpty());

        newcastleVacancy.getVacancyLocations().add(bristolLocation1);
        bristolLocation1.setVacancy(newcastleVacancy);
        vacancyRepository.save(newcastleVacancy);

        result = findVancanciesInPlace("bristol");
        resultsList = result.getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());
    }

    @Test
    public void testFilterBristolVacancies() throws Exception {

        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        createVacancyWithRegions(department1, "South West, Scotland", bristolLocation1);
        createVacancyWithRegions(department1, "North East, Scotland", newcastleLocation);


        Page<Vacancy> result = findVancanciesInPlace("bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());
        Assert.assertEquals("Expected number results", 1, resultsList.size());
    }

    @Test
    public void testFilterByDepartment() throws Exception {

        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        createVacancyWithRegions(department1, "South West, Scotland", bristolLocation1);
        createVacancyWithRegions(department2, "North East, Scotland", bristolLocation2);


        // return both departments
        Page<Vacancy> result = findVancanciesByDpartmentInPlace("bristol", 
                department1.getId().toString(), 
                department2.getId().toString());

        List<Vacancy> resultsList = result.getContent();

        Assert.assertEquals("Expected number results", 2, resultsList.size());

        // dont filter by any departments
        result = findVancanciesByDpartmentInPlace("bristol");
        resultsList = result.getContent();
        Assert.assertEquals("Expected number results", 2, resultsList.size());

        // filter by  department 1
        result = findVancanciesByDpartmentInPlace("bristol", department1.getId().toString());
        resultsList = result.getContent();
        Assert.assertEquals("Expected number results", 1, resultsList.size());
        Assert.assertEquals("Department1.id", resultsList.get(0).getDepartment().getId(), department1.getId());

        // filter by  department 2
        result = findVancanciesByDpartmentInPlace("bristol", department2.getId().toString());
        resultsList = result.getContent();
        Assert.assertEquals("Expected number results", 1, resultsList.size());
        Assert.assertEquals("Department1.id", resultsList.get(0).getDepartment().getId(), department2.getId());

        // filter by  unknown departent
        result = findVancanciesByDpartmentInPlace("bristol", "-1");
        resultsList = result.getContent();
        Assert.assertEquals("Expected number results", 0, resultsList.size());
    }

    @Test
    public void testFindRegionalVacanciesNewcastle() throws Exception {

        given(locationService.find("newcastle"))
                .willReturn(new Coordinates(NEWCASTLE_LONGITUDE, NEWCASTLE_LATITUDE, "North East"));

        createVacancyWithRegions(department1, "North East, Scotland", newcastleLocation);
        createVacancyWithRegions(department1, "South West, Scotland, North East", bristolLocation1);

        Page<Vacancy> result = findVancanciesInPlace("newcastle");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertEquals("Expected results 2", 2, resultsList.size());
    }

    @Test
    public void testGetClosedVacancy() throws Exception {

        given(locationService.find(any()))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        Vacancy closedVacancy = createVacancyWithClosingDate("yesterday", YESTERDAY, department1);

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

        given(locationService.find(any()))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        ObjectMapper objectMapper = new ObjectMapper();

        Vacancy closedVacancy = createVacancyWithClosingDate("yesterday", YESTERDAY, department1);
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
                department1.getDisabilityLogo());
    }

    @Test
    public void testLocationServiceUnavailable() throws LocationServiceException, Exception {

        given(locationService.find(any())).willThrow(new LocationServiceException());

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
                .andExpect(status().isServiceUnavailable())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        VacancyError vacancyError = objectMapper.readValue(response, VacancyError.class);

        assertThat(vacancyError.getStatus(), is(HttpStatus.SERVICE_UNAVAILABLE));
        assertThat(vacancyError.getMessage(),
                containsString(LocationServiceException.SERVICE_UNAVAILABLE_MESSAGE));

        Mockito.reset(locationService);
    }

    @Test
    public void testRuntimeException() throws LocationServiceException, Exception {

        String errorMessage = "bad times";

        given(locationService.find(any())).willThrow(new RuntimeException(errorMessage));

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
                .andExpect(status().isInternalServerError())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        VacancyError vacancyError = objectMapper.readValue(response, VacancyError.class);

        assertThat(vacancyError.getStatus(), is(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(vacancyError.getMessage(), containsString(errorMessage));

        Mockito.reset(locationService);
    }

    @Test
    public void search_salaryOverrideDescriptionReturned() throws Exception {

        Vacancy vacancy = createVacancyPrototype(bristolLocation1);

        vacancyRepository.save(vacancy);

        given(locationService.find("testLocation1"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        Page<Vacancy> result = findVancanciesInPlace("testLocation1");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", resultsList.size() == 1);
        Vacancy actual = resultsList.get(0);

        assertThat(actual.getSalaryOverrideDescription(), equalTo("This is the salary override description"));
    }

    private Page<Vacancy> findVancanciesByKeyword(String keyword) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword(keyword)
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

        System.out.println("searchRespons=" + searchResponse);

        return objectMapper.readValue(searchResponse, VacancyPage.class);
    }

    private Page<Vacancy> findVancanciesByDpartmentInPlace(String place, String... departmentIDs) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("SearchQueryDescription")
                .overseasJob(Boolean.TRUE)
                .department(departmentIDs)
                .location(new Location(place, 30))
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

        System.out.println("searchRespons=" + searchResponse);

        return objectMapper.readValue(searchResponse, VacancyPage.class);
    }

    private Page<Vacancy> findVancanciesInPlace(String place) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("SearchQueryDescription")
                .overseasJob(Boolean.TRUE)
                .location(new Location(place, 30))
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

        System.out.println("searchRespons=" + searchResponse);

        return objectMapper.readValue(searchResponse, VacancyPage.class);
    }

    private static Timestamp getTime(int numberOfDaysFromNow) {
        Date date = Date.from(LocalDateTime.now().plusDays(numberOfDaysFromNow).atZone(ZoneId.systemDefault()).toInstant());
        return new Timestamp(date.getTime());
    }

    private Vacancy createVacancyWithRegions(Department department1, String regions, VacancyLocation vacancyLocation) {

        Vacancy vacancy = createVacancyPrototype(vacancyLocation);
        vacancy.setRegions(regions);
        vacancy.setDepartment(department1);
        vacancyRepository.save(vacancy);
        return vacancy;
    }

    private Vacancy createVacancyWithClosingDate(String jobTitle, Timestamp closingDate, Department department1) {

        VacancyLocation vacancyLocation = VacancyLocation.builder()
                .latitude(BRISTOL_LATITUDE)
                .longitude(BRISTOL_LONGITUDE)
                .location("testLocation1 SearchQueryLocation")
                .build();

        Vacancy vacancy = createVacancyPrototype(vacancyLocation);
        vacancy.setTitle(jobTitle);

        vacancy.setDepartment(department1);
        vacancy.setClosingDate(closingDate);
        vacancyRepository.save(vacancy);
        return vacancy;
    }

    private Vacancy saveVacancy(Vacancy vacancy) {

        vacancyRepository.save(vacancy);
        return vacancy;
    }

    private Vacancy createVacancyPrototype(VacancyLocation vacancyLocation) {

        Vacancy vacancy = Vacancy.builder()
                .department(department1)
                .title("testTile1 SearchQueryTitle")
                .description("testDescription1 SearchQueryDescription")
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
                .identifier(System.currentTimeMillis())
                .salaryOverrideDescription("This is the salary override description")
                .overseasJob(Boolean.FALSE)
                .build();

        vacancyLocation.setVacancy(vacancy);
        vacancy.setVacancyLocations(new ArrayList<>());
        vacancy.getVacancyLocations().add(vacancyLocation);
        return vacancy;
    }
}
