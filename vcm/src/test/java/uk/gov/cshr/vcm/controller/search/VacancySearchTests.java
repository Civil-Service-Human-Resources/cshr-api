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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.controller.SearchResponsePage;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.controller.exception.SearchStatusCode;
import uk.gov.cshr.vcm.controller.exception.VacancyClosedException;
import uk.gov.cshr.vcm.controller.exception.VacancyError;
import uk.gov.cshr.vcm.model.ContractType;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.EmailExtension;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyLocation;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.model.VerifyResponse;
import uk.gov.cshr.vcm.model.WorkingPattern;
import uk.gov.cshr.vcm.repository.DepartmentRepository;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.ApplicantTrackingSystemService;
import uk.gov.cshr.vcm.service.CshrAuthenticationService;
import uk.gov.cshr.vcm.service.HibernateSearchService;
import uk.gov.cshr.vcm.service.LocationService;
import uk.gov.cshr.vcm.service.NotifyService;
import uk.gov.cshr.vcm.service.SearchService;

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

	@Inject
	private SearchService searchService;

	@Inject
	private CshrAuthenticationService cshrAuthenticationService;

    private MockMvc mockMvc;

    @MockBean
    private ApplicantTrackingSystemService applicantTrackingSystemService;

    @MockBean
    private LocationService locationService;

    @MockBean
    private NotifyService notifyService;

    @Inject
    private HibernateSearchService hibernateSearchService;

    private static final Timestamp YESTERDAY = getTime(-1);
    private static final Timestamp TODAY = getTime(0);
    private static final Timestamp TOMORROW = getTime(+1);
    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30);

    private Department department1;
    private Department department2;
    private Department parentDepartment;
    private Department childDepartment;
    private Department siblingDepartment;


    private final VacancyLocation newcastleLocation = VacancyLocation.builder()
            .latitude(NEWCASTLE_LATITUDE)
            .longitude(NEWCASTLE_LONGITUDE)
            .location("Newcastle")
            .build();

    private final VacancyLocation newcastleLocation2 = VacancyLocation.builder()
            .latitude(NEWCASTLE_LATITUDE)
            .longitude(NEWCASTLE_LONGITUDE)
            .location("Newcastle2")
            .build();

    private final VacancyLocation newcastleLocation3 = VacancyLocation.builder()
            .latitude(NEWCASTLE_LATITUDE)
            .longitude(NEWCASTLE_LONGITUDE)
            .location("Newcastle3")
            .build();

    private VacancyLocation createBristolLocationPrototype(String locationName) {
        return VacancyLocation.builder()
            .latitude(BRISTOL_LATITUDE)
            .longitude(BRISTOL_LONGITUDE)
            .location(locationName)
            .build();
    }

    @Before
    public void before() throws LocationServiceException {

        hibernateSearchService.purge();
        vacancyRepository.deleteAll();
        departmentRepository.deleteAll();;

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        department1 = Department.builder()
                        .name("Department One")
                        .disabilityLogo("disabilityLogo")
                        .build();

        EmailExtension emailExtension = EmailExtension.builder()
                                        .department(department1)
                                        .emailExtension("cabinetoffice.gov.uk")
                                        .build();

        department1.getAcceptedEmailExtensions().add(emailExtension);
        department1 = departmentRepository.save(department1);

        department1 = departmentRepository.save(department1);

        department2 = departmentRepository.save(
                Department.builder()
                        .name("Department two")
                        .disabilityLogo("disabilityLogo")
                        .build());

        parentDepartment = departmentRepository.save(
                Department.builder()
                        .name("Parent Department")
                        .disabilityLogo("disabilityLogo")
                        .build());

        EmailExtension parentDepartmentEmail = EmailExtension.builder()
                                        .department(parentDepartment)
                                        .emailExtension("parentdepartment@email.com")
                                        .build();
        parentDepartment.getAcceptedEmailExtensions().add(parentDepartmentEmail);
        departmentRepository.save(parentDepartment);

        childDepartment = departmentRepository.save(
                Department.builder()
                        .name("Child Department")
                        .disabilityLogo("disabilityLogo")
                        .build());

        siblingDepartment = departmentRepository.save(
                Department.builder()
                        .name("Grand Child Department")
                        .disabilityLogo("disabilityLogo")
                        .parent(parentDepartment)
                        .build());

        EmailExtension childDepartmentEmail = EmailExtension.builder()
                                        .department(parentDepartment)
                                        .emailExtension("childdepartment@email.com")
                                        .build();
        childDepartment.getAcceptedEmailExtensions().add(childDepartmentEmail);
        childDepartment.setParent(parentDepartment);
        departmentRepository.save(childDepartment);

    }

    @After
    public void after() {
    }

    @Test
    public void testExcludeClosedVacancies() throws Exception {

		Date now = new Date();

        given(locationService.find(any()))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        createVacancyWithClosingDate("yesterday", YESTERDAY, department1);

        // beacuse these are timestamp basesd this job will be closed
        Timestamp earlierToday = new Timestamp(now.getTime() - 60000);
        createVacancyWithClosingDate("earlier today", earlierToday, department1);

        createVacancyWithClosingDate("tomorrow", TOMORROW, department1);
        createVacancyWithClosingDate("thirty days time", THIRTY_DAYS_FROM_NOW, department1);

        SearchResponsePage result = findVancanciesInPlace("bristol");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());

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

        SearchResponsePage result = findVancanciesByKeyword("newcastle");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("Newcastle Job", resultsList.get(0).getTitle());

        Vacancy bristolVacancy = createVacancyPrototype(createBristolLocationPrototype("bristol1"));
        bristolVacancy.setTitle("Bristol Job");
        saveVacancy(bristolVacancy);

        result = findVancanciesByKeyword("newcastle");
        resultsList = result.getVacancies().getContent();

        Assert.assertEquals("Newcastle Job", resultsList.get(0).getTitle());
        Assert.assertEquals("1", 1, resultsList.size());
    }

    @Test
    public void testExcludeInternalVacancy() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
		newcastleVacancy.setInternalOpeningDate(YESTERDAY);
		newcastleVacancy.setPublicOpeningDate(YESTERDAY);
        newcastleVacancy.setTitle("Newcastle Job");
        saveVacancy(newcastleVacancy);

        Vacancy newcastleVacancy2 = createVacancyPrototype(newcastleLocation2);
		newcastleVacancy2.setInternalOpeningDate(YESTERDAY);
		newcastleVacancy2.setPublicOpeningDate(TOMORROW);
        newcastleVacancy2.setTitle("Newcastle Job 2");
        saveVacancy(newcastleVacancy2);

        SearchResponsePage result = findVancanciesByKeyword("newcastle");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("internal vacancy excluded", 1, resultsList.size());
        Assert.assertEquals("Newcastle Job", resultsList.get(0).getTitle());
    }

    @Test
    public void findParentDepartments() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
        newcastleVacancy.setDepartment(parentDepartment);
		newcastleVacancy.setGovernmentOpeningDate(TOMORROW);
		newcastleVacancy.setPublicOpeningDate(TOMORROW);
        newcastleVacancy.setInternalOpeningDate(YESTERDAY);
        newcastleVacancy.setTitle("Parent Vacancy");
        saveVacancy(newcastleVacancy);

        Vacancy newcastleVacancy2 = createVacancyPrototype(newcastleLocation2);
        newcastleVacancy2.setDepartment(childDepartment);
		newcastleVacancy2.setGovernmentOpeningDate(TOMORROW);
		newcastleVacancy2.setPublicOpeningDate(TOMORROW);
        newcastleVacancy2.setInternalOpeningDate(YESTERDAY);
        newcastleVacancy2.setTitle("Child Vacancy");
        saveVacancy(newcastleVacancy2);

        Vacancy newcastleVacancy3 = createVacancyPrototype(newcastleLocation3);
        newcastleVacancy3.setDepartment(siblingDepartment);
		newcastleVacancy3.setGovernmentOpeningDate(TOMORROW);
		newcastleVacancy3.setPublicOpeningDate(TOMORROW);
        newcastleVacancy3.setInternalOpeningDate(YESTERDAY);
        newcastleVacancy3.setTitle("Sibling Vacancy");
        saveVacancy(newcastleVacancy3);

        // a candiate with a parent email should also match jobs in the child departments
        String jwt = cshrAuthenticationService.createInternalJWT("parentdepartment@email.com", parentDepartment);

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .build();

        SearchResponsePage result = findVancanciesByKeyword(vacancySearchParameters, jwt);
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals(3, resultsList.size());
    }

    @Test
    public void testIncludeAcrossGovernmentVacancy() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
		newcastleVacancy.setGovernmentOpeningDate(YESTERDAY);
		newcastleVacancy.setPublicOpeningDate(TOMORROW);
        newcastleVacancy.setTitle("Newcastle Job");
        saveVacancy(newcastleVacancy);

        Vacancy newcastleVacancy2 = createVacancyPrototype(newcastleLocation2);
		newcastleVacancy2.setGovernmentOpeningDate(YESTERDAY);
		newcastleVacancy2.setPublicOpeningDate(TOMORROW);
        newcastleVacancy2.setTitle("Newcastle Job 2");
        saveVacancy(newcastleVacancy2);

		String jwt = cshrAuthenticationService.createInternalJWT("cabinetoffice.gov.uk", department2);
		System.out.println("JWT=" + jwt);

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("newcastle")
                .build();

        SearchResponsePage result = findVancanciesByKeyword(vacancySearchParameters, jwt);
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("internal vacancy included", 2, resultsList.size());
    }

    @Test
    public void testPublicDateBeforeAcrossGovernment() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
		newcastleVacancy.setGovernmentOpeningDate(YESTERDAY);
		newcastleVacancy.setPublicOpeningDate(YESTERDAY);
        newcastleVacancy.setTitle("Newcastle Job");
        saveVacancy(newcastleVacancy);

        Vacancy newcastleVacancy2 = createVacancyPrototype(newcastleLocation2);
		newcastleVacancy2.setGovernmentOpeningDate(YESTERDAY);
		newcastleVacancy2.setPublicOpeningDate(TOMORROW);
        newcastleVacancy2.setTitle("Newcastle Job 2");
        saveVacancy(newcastleVacancy2);

		String jwt = cshrAuthenticationService.createInternalJWT("cabinetoffice.gov.uk", department1);
		System.out.println("JWT=" + jwt);

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("newcastle")
                .build();

        SearchResponsePage result = findVancanciesByKeyword(vacancySearchParameters, jwt);
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("internal vacancy included", 2, resultsList.size());
    }

    @Test
    public void testPublicDateBeforeInternalGovernment() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
		newcastleVacancy.setPublicOpeningDate(YESTERDAY);
		newcastleVacancy.setInternalOpeningDate(TOMORROW);
        newcastleVacancy.setTitle("Newcastle Job");
        saveVacancy(newcastleVacancy);

        Vacancy newcastleVacancy2 = createVacancyPrototype(newcastleLocation2);
		newcastleVacancy2.setPublicOpeningDate(YESTERDAY);
		newcastleVacancy2.setInternalOpeningDate(TOMORROW);
        newcastleVacancy2.setTitle("Newcastle Job 2");
        saveVacancy(newcastleVacancy2);

		String jwt = cshrAuthenticationService.createInternalJWT("cabinetoffice.gov.uk", department1);
		System.out.println("JWT=" + jwt);

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("newcastle")
                .build();

        SearchResponsePage result = findVancanciesByKeyword(vacancySearchParameters, jwt);
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals(2, resultsList.size());
    }

    @Test
    public void testExcludeInternalVacancyByDepartment() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
        newcastleVacancy.setDepartment(department1);
		newcastleVacancy.setGovernmentOpeningDate(TOMORROW);
		newcastleVacancy.setPublicOpeningDate(TOMORROW);
        newcastleVacancy.setInternalOpeningDate(YESTERDAY);
        newcastleVacancy.setTitle("Newcastle Job");
        saveVacancy(newcastleVacancy);

        Vacancy newcastleVacancy2 = createVacancyPrototype(newcastleLocation2);
        newcastleVacancy2.setDepartment(department2);
		newcastleVacancy2.setGovernmentOpeningDate(TOMORROW);
		newcastleVacancy2.setPublicOpeningDate(TOMORROW);
        newcastleVacancy2.setInternalOpeningDate(YESTERDAY);
        newcastleVacancy2.setTitle("Newcastle Job 2");
        saveVacancy(newcastleVacancy2);

		String jwt = cshrAuthenticationService.createInternalJWT("cabinetoffice.gov.uk", department2);
		System.out.println("JWT=" + jwt);

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("newcastle")
                .build();

        SearchResponsePage result = findVancanciesByKeyword(vacancySearchParameters, jwt);
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("department2 vacancy included", 1, resultsList.size());
        Assert.assertEquals("Newcastle Job 2", resultsList.get(0).getTitle());
    }

    @Test
    public void testHandleInvalidJWTOnInternalVacancySearch() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
		newcastleVacancy.setInternalOpeningDate(YESTERDAY);
		newcastleVacancy.setPublicOpeningDate(YESTERDAY);
        newcastleVacancy.setTitle("Newcastle Job");
        saveVacancy(newcastleVacancy);

        Vacancy newcastleVacancy2 = createVacancyPrototype(newcastleLocation2);
		newcastleVacancy2.setInternalOpeningDate(YESTERDAY);
		newcastleVacancy2.setPublicOpeningDate(TOMORROW);
        newcastleVacancy2.setTitle("Newcastle Job 2");
        saveVacancy(newcastleVacancy2);

		String jwt = cshrAuthenticationService.createInternalJWT("cabinetoffice.gov.uk", department1);
		System.out.println("JWT=" + jwt);

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("newcastle")
                .build();

        SearchResponsePage result = findVancanciesByKeyword(vacancySearchParameters, "jwt");

        List<VacancyError> vacancyError = result.getVacancyErrors();
        Assert.assertEquals("", vacancyError.get(0).getSearchStatusCode(), SearchStatusCode.INVALID_JWT);

        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("internal vacancy included", 1, resultsList.size());
        Assert.assertEquals("Newcastle Job", resultsList.get(0).getTitle());
    }

    @Test
    public void testHandleUnauthorisedEmailOnVerifyEmail() throws Exception {

        String requestBody = "{ \"emailAddress\": \"anyone@yahoo.co.uk\" }";

        MvcResult mvcResult = this.mockMvc.perform(post("/vacancy/verifyemail")
				.with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestBody)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        VerifyResponse verifyResponse = new ObjectMapper().readValue(response, VerifyResponse.class);
        Assert.assertEquals("Expect UNAUTHORIZED error status",
                HttpStatus.UNAUTHORIZED,
                verifyResponse.getVacancyError().getStatus());
    }

    @Test
    public void testInvalidLocation() throws Exception {

        given(locationService.find(any()))
            .willReturn(null);

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("any")
                .location(Location.builder()
                        .place("london/?hgff8987")
                        .radius(30)
                        .build())
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
    }

    @Test
    public void testHandleInvalidCharsInSearch() throws Exception {

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("£%^&*")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(vacancySearchParameters);

        this.mockMvc.perform(post("/vacancy/search")
				.with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testFilterInactiveVacancies() throws Exception {

        Vacancy vacancy1 = createVacancyPrototype(newcastleLocation);
		vacancy1.setActive(false);
        vacancy1.setTitle("Newcastle Job");
        saveVacancy(vacancy1);

        Vacancy vacancy2 = createVacancyPrototype(newcastleLocation2);
		vacancy2.setActive(false);
        vacancy2.setTitle("Second Newcastle Job");
        saveVacancy(vacancy2);

        Vacancy vacancy3 = createVacancyPrototype(newcastleLocation3);
		vacancy3.setActive(true);
        vacancy3.setTitle("third Newcastle Job");
        saveVacancy(vacancy3);

        SearchResponsePage result = findVancanciesByKeyword("newcastle");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertTrue("Results expected", resultsList.size() == 1);

        for (Vacancy vacancy : resultsList) {
			Assert.assertTrue("Returned vacancy is active", vacancy.getActive());
		}
    }

    @Test
    public void testIndexUpdated() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
        newcastleVacancy.setTitle("Newcastle Job");
        saveVacancy(newcastleVacancy);

        SearchResponsePage result = findVancanciesByKeyword("newcastle");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("1", 1, resultsList.size());
        Assert.assertEquals("Newcastle Job", resultsList.get(0).getTitle());

        ObjectMapper objectMapper = new ObjectMapper();

        newcastleVacancy.setTitle("findthis");
        ResultActions sendRequest = this.mockMvc.perform(put("/vacancy/" + newcastleVacancy.getId())
				.with(user("searchusername").password("searchpassword").roles("CRUD_ROLE"))
				.contentType(APPLICATION_JSON_UTF8).content(objectMapper.writeValueAsString(newcastleVacancy)));

        sendRequest.andExpect(status().isOk());

        result = findVancanciesByKeyword("findthis");
        resultsList = result.getVacancies().getContent();

        Assert.assertEquals("1", 1, resultsList.size());
        Assert.assertEquals("findthis", resultsList.get(0).getTitle());

        newcastleVacancy.setTitle("newtitle");
        sendRequest = this.mockMvc.perform(post("/vacancy/save/")
				.with(user("searchusername").password("searchpassword").roles("CRUD_ROLE"))
				.contentType(APPLICATION_JSON_UTF8).content(objectMapper.writeValueAsString(newcastleVacancy)));

        sendRequest.andExpect(status().isOk());

        result = findVancanciesByKeyword("newtitle");
        resultsList = result.getVacancies().getContent();

        Assert.assertEquals("1", 1, resultsList.size());
        Assert.assertEquals("newtitle", resultsList.get(0).getTitle());
    }

    @Test
    public void testNoResults() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
        newcastleVacancy.setTitle("Newcastle Job");
        saveVacancy(newcastleVacancy);

        SearchResponsePage result = findVancanciesByKeyword("zxcvbnm");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("Expect no results", 0, resultsList.size());
    }

    @Test
    public void testFindSynonymFromFullNameSearch() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
        newcastleVacancy.setTitle("GDS Engineer");
        saveVacancy(newcastleVacancy);

        SearchResponsePage result = findVancanciesByKeyword("Government Digital Services");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("Expect one result", 1, resultsList.size());
        Assert.assertEquals("Find GDS Engineer", "GDS Engineer", resultsList.get(0).getTitle());
    }

    @Test
    public void testFindFullNameFromSynonymSearch() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
        newcastleVacancy.setTitle("International Business Machines");
        saveVacancy(newcastleVacancy);

        SearchResponsePage result = findVancanciesByKeyword("IBM Manager");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("Expect one result", 1, resultsList.size());
        Assert.assertEquals("Find International Business Machines", "International Business Machines",
                resultsList.get(0).getTitle());
    }

    @Test
    public void testTitleMoreRelevantThanDescription() throws Exception {

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
        newcastleVacancy.setTitle("any old title");
        newcastleVacancy.setDescription("jobs in newcastle are great");
        saveVacancy(newcastleVacancy);

        Vacancy newcastleVacancy2 = createVacancyPrototype(newcastleLocation2);
        newcastleVacancy2.setTitle("Newcastle Vacancy");
        newcastleVacancy2.setDescription("jobs in are great");
        saveVacancy(newcastleVacancy2);

        SearchResponsePage result = findVancanciesByKeyword("newcastle");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("Expect two results", 2, resultsList.size());
        Assert.assertEquals("title match is first", newcastleVacancy2.getId(),
                resultsList.get(0).getId());
    }

    @Test
    public void testFindMultipleLocations() throws Exception {

        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        Vacancy newcastleVacancy = createVacancyPrototype(newcastleLocation);
        saveVacancy(newcastleVacancy);

        SearchResponsePage result = findVancanciesInPlace("bristol");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertTrue("Expected no results", resultsList.isEmpty());

        VacancyLocation vacancyLocation = createBristolLocationPrototype("bristol1");
        newcastleVacancy.getVacancyLocations().add(vacancyLocation);
        vacancyLocation.setVacancy(newcastleVacancy);
        vacancyRepository.save(newcastleVacancy);

        result = findVancanciesInPlace("bristol");
        resultsList = result.getVacancies().getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());
    }

    @Test
    public void testFilterBristolVacancies() throws Exception {

        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        createVacancyWithRegions(department1, "South West, Scotland", createBristolLocationPrototype("bristol1"));
        createVacancyWithRegions(department1, "North East, Scotland", newcastleLocation);


        SearchResponsePage result = findVancanciesInPlace("bristol");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());
        Assert.assertEquals("Expected number results", 1, resultsList.size());
    }

    @Test
    public void testFilterByDepartment() throws Exception {

        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        createVacancyWithRegions(department1, "South West, Scotland", createBristolLocationPrototype("bristol1"));
        createVacancyWithRegions(department2, "North East, Scotland", createBristolLocationPrototype("bristol2"));


        // return both departments
        SearchResponsePage result = findVancanciesByDpartmentInPlace("bristol",
                department1.getId().toString(),
                department2.getId().toString());

        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("Expected number results", 2, resultsList.size());

        // dont filter by any departments
        result = findVancanciesByDpartmentInPlace("bristol");
        resultsList = result.getVacancies().getContent();
        Assert.assertEquals("Expected number results", 2, resultsList.size());

        // filter by  department 1
        result = findVancanciesByDpartmentInPlace("bristol", department1.getId().toString());
        resultsList = result.getVacancies().getContent();
        Assert.assertEquals("Expected number results", 1, resultsList.size());
        Assert.assertEquals("Department1.id", resultsList.get(0).getDepartment().getId(), department1.getId());

        // filter by  department 2
        result = findVancanciesByDpartmentInPlace("bristol", department2.getId().toString());
        resultsList = result.getVacancies().getContent();
        Assert.assertEquals("Expected number results", 1, resultsList.size());
        Assert.assertEquals("Department1.id", resultsList.get(0).getDepartment().getId(), department2.getId());

        // filter by  unknown departent
        result = findVancanciesByDpartmentInPlace("bristol", "-1");
        resultsList = result.getVacancies().getContent();
        Assert.assertEquals("Expected number results", 0, resultsList.size());
    }

    @Test
    public void testFilterByContractType() throws Exception {

        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        Vacancy fulltimeVacancy = createVacancyPrototype(createBristolLocationPrototype("bristol1"));
        fulltimeVacancy.setContractTypes(ContractType.FULL_TIME.toString() + ", anythingelse1");
        saveVacancy(fulltimeVacancy);

        Vacancy parttimeVacancy = createVacancyPrototype(createBristolLocationPrototype("bristol1"));
        parttimeVacancy.setContractTypes(ContractType.PART_TIME.toString() + ", anythingelse2");
        saveVacancy(parttimeVacancy);

        Vacancy internshipVacancy = createVacancyPrototype(createBristolLocationPrototype("bristol1"));
        internshipVacancy.setContractTypes(ContractType.INTERNSHIP.toString() + ", anythingelse3");
        saveVacancy(internshipVacancy);

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .build();

        // return all three
        SearchResponsePage result = findVancancies(vacancySearchParameters);
        List<Vacancy> resultsList = result.getVacancies().getContent();
        Assert.assertEquals("Expected number results", 3, resultsList.size());

        vacancySearchParameters = VacancySearchParameters.builder()
                .contractTypes(new String[] {
                    ContractType.SEASONAL.toString(),
                })
                .build();

        // no vacancies should exist matching seasonal
        result = findVancancies(vacancySearchParameters);
        resultsList = result.getVacancies().getContent();
        Assert.assertEquals("Expected number results", 0, resultsList.size());

        vacancySearchParameters = VacancySearchParameters.builder()
                .contractTypes(new String[] {
                    ContractType.INTERNSHIP.toString(),
                })
                .build();

        // One vacancy sholud match internship
        result = findVancancies(vacancySearchParameters);
        resultsList = result.getVacancies().getContent();
        Assert.assertEquals("Expected number results", 1, resultsList.size());
        Assert.assertTrue("Expected result", resultsList.get(0).getContractTypes().contains(ContractType.INTERNSHIP.toString()));

        vacancySearchParameters = VacancySearchParameters.builder()
                .contractTypes(new String[] {
                    ContractType.FULL_TIME.toString(),
                    ContractType.PART_TIME.toString(),
                })
                .build();

        // two vacancies should exist matching full/parttime
        result = findVancancies(vacancySearchParameters);
        resultsList = result.getVacancies().getContent();

        Assert.assertEquals("Expected number results", 2, resultsList.size());

        for (Vacancy vacancy : resultsList) {
            Assert.assertTrue(vacancy.getContractTypes().contains(ContractType.FULL_TIME.toString())
            || vacancy.getContractTypes().contains(ContractType.PART_TIME.toString() ));
        }
    }

    @Test
    public void testFilterByWorkingPattern() throws Exception {

        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        Vacancy flexibleWorkingVacancy = createVacancyPrototype(createBristolLocationPrototype("bristol1"));
        flexibleWorkingVacancy.setWorkingPatterns(WorkingPattern.FLEXIBLE_WORKING.toString() + ", anythingelse1");
        saveVacancy(flexibleWorkingVacancy);

        Vacancy fullTimeVacancy = createVacancyPrototype(createBristolLocationPrototype("bristol1"));
        fullTimeVacancy.setWorkingPatterns(WorkingPattern.FULL_TIME.toString() + ", anythingelse2");
        saveVacancy(fullTimeVacancy);

        Vacancy homeWorkingVacancy = createVacancyPrototype(createBristolLocationPrototype("bristol1"));
        homeWorkingVacancy.setWorkingPatterns(WorkingPattern.HOME_WORKING.toString() + ", anythingelse3");
        saveVacancy(homeWorkingVacancy);

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .build();

        // return all three
        SearchResponsePage result = findVancancies(vacancySearchParameters);
        List<Vacancy> resultsList = result.getVacancies().getContent();
        Assert.assertEquals("Expected number results", 3, resultsList.size());

        vacancySearchParameters = VacancySearchParameters.builder()
                .workingPatterns(new String[] {
                    WorkingPattern.JOB_SHARE.toString(),
                })
                .build();

        // no vacancies should exist matching JOB_SHARE
        result = findVancancies(vacancySearchParameters);
        resultsList = result.getVacancies().getContent();
        Assert.assertEquals("Expected number results", 0, resultsList.size());

        vacancySearchParameters = VacancySearchParameters.builder()
                .workingPatterns(new String[] {
                    WorkingPattern.FLEXIBLE_WORKING.toString(),
                })
                .build();

        // One vacancy should exist matching FLEXIBLE_WORKING
        result = findVancancies(vacancySearchParameters);
        resultsList = result.getVacancies().getContent();
        Assert.assertEquals("Expected number results", 1, resultsList.size());
        Assert.assertTrue("Expected result", resultsList.get(0).getWorkingPatterns().contains(WorkingPattern.FLEXIBLE_WORKING.toString()));

        vacancySearchParameters = VacancySearchParameters.builder()
                .workingPatterns(new String[] {
                    WorkingPattern.FLEXIBLE_WORKING.toString(),
                    WorkingPattern.FULL_TIME.toString(),
                })
                .build();

        // two vacancies should exist matching full/parttime
        result = findVancancies(vacancySearchParameters);
        resultsList = result.getVacancies().getContent();

        Assert.assertEquals("Expected number results", 2, resultsList.size());

        for (Vacancy vacancy : resultsList) {
            Assert.assertTrue(vacancy.getWorkingPatterns().contains(WorkingPattern.FLEXIBLE_WORKING.toString())
            || vacancy.getWorkingPatterns().contains(WorkingPattern.FULL_TIME.toString() ));
        }
    }

    @Test
    public void testFindRegionalVacanciesNewcastle() throws Exception {

        given(locationService.find("newcastle"))
                .willReturn(new Coordinates(NEWCASTLE_LONGITUDE, NEWCASTLE_LATITUDE, "North East"));

        createVacancyWithRegions(department1, "North East, Scotland", newcastleLocation);
        createVacancyWithRegions(department1, "South West, Scotland, North East", createBristolLocationPrototype("bristol1"));

        SearchResponsePage result = findVancanciesInPlace("newcastle");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertEquals("Expected results 2", 2, resultsList.size());
    }

    @Test
    public void testGetClosedVacancy() throws Exception {

        given(locationService.find(any()))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        Vacancy closedVacancy = createVacancyWithClosingDate("yesterday", YESTERDAY, department1);

        MvcResult mvcResult = this.mockMvc.perform(get("/vacancy/" + closedVacancy.getId())
				.with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
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
    public void testGetInactiveVacancy() throws Exception {

        given(locationService.find(any()))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        Vacancy inactiveVacancy = createVacancyPrototype(newcastleLocation);
		inactiveVacancy.setActive(false);
        inactiveVacancy.setTitle("Newcastle Job");
        saveVacancy(inactiveVacancy);

        MvcResult mvcResult = this.mockMvc.perform(get("/vacancy/" + inactiveVacancy.getId())
				.with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isGone())
                .andReturn();
    }

    @Test
    public void testUpdateClosedVacancy() throws Exception {

        given(locationService.find(any()))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        ObjectMapper objectMapper = new ObjectMapper();

        Vacancy closedVacancy = createVacancyWithClosingDate("yesterday", YESTERDAY, department1);
        closedVacancy.setClosingDate(THIRTY_DAYS_FROM_NOW);

        MvcResult mvcUpdateResult = this.mockMvc.perform(put("/vacancy/" + closedVacancy.getId())
				.with(user("searchusername").password("searchpassword").roles("CRUD_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(closedVacancy))
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcFindResult = this.mockMvc.perform(get("/vacancy/" + closedVacancy.getId())
				.with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
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
				.with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
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
				.with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
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

        Vacancy vacancy = createVacancyPrototype(createBristolLocationPrototype("bristol1"));

        vacancyRepository.save(vacancy);

        given(locationService.find("testLocation1"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        SearchResponsePage result = findVancanciesInPlace("testLocation1");
        List<Vacancy> resultsList = result.getVacancies().getContent();

        Assert.assertTrue("Expected results", resultsList.size() == 1);
        Vacancy actual = resultsList.get(0);

        assertThat(actual.getSalaryOverrideDescription(), equalTo("This is the salary override description"));
    }

    private SearchResponsePage findVancancies(VacancySearchParameters vacancySearchParameters) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

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

        System.out.println("searchRespons=" + searchResponse);

        return objectMapper.readValue(searchResponse, SearchResponsePage.class);
    }

    private SearchResponsePage findVancanciesByKeyword(VacancySearchParameters vacancySearchParameters, String jwt)
			throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(vacancySearchParameters);

        MvcResult mvcResult = this.mockMvc.perform(post("/vacancy/search")
				.with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
				.header("cshr-authentication", jwt)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        String searchResponse = mvcResult.getResponse().getContentAsString();

        System.out.println("searchRespons=" + searchResponse);

        return objectMapper.readValue(searchResponse, SearchResponsePage.class);
    }

    private SearchResponsePage findVancanciesByKeyword(String keyword) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword(keyword)
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

        System.out.println("searchRespons=" + searchResponse);

        return objectMapper.readValue(searchResponse, SearchResponsePage.class);
    }

    private SearchResponsePage findVancanciesByDpartmentInPlace(String place, String... departmentIDs) throws Exception {

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
				.with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        String searchResponse = mvcResult.getResponse().getContentAsString();

        System.out.println("searchRespons=" + searchResponse);

        return objectMapper.readValue(searchResponse, SearchResponsePage.class);
    }

    private SearchResponsePage findVancanciesInPlace(String place) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("SearchQueryDescription")
                .overseasJob(Boolean.TRUE)
                .location(new Location(place, 30))
                .build();

        String json = objectMapper.writeValueAsString(vacancySearchParameters);

        MvcResult mvcResult = this.mockMvc.perform(post("/vacancy/search")
				.with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        String searchResponse = mvcResult.getResponse().getContentAsString();

        System.out.println("searchRespons=" + searchResponse);

        return objectMapper.readValue(searchResponse, SearchResponsePage.class);
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
