package uk.gov.cshr.vcm.controller;

import static java.lang.Math.toIntExact;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.ContractType;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.NationalityStatement;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyLocation;
import uk.gov.cshr.vcm.model.WorkingPattern;
import uk.gov.cshr.vcm.repository.DepartmentRepository;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.LocationService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class VacancyControllerTest extends AbstractTestNGSpringContextTests {
    private static final SimpleDateFormat ISO_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    static {
        ISO_DATEFORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private static final double BRISTOL_LATITUDE = 51.4549291;
    private static final double BRISTOL_LONGITUDE = -2.6278111;

    private static final int TEN_DAYS_AGO = -10;
    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30);
    private static final int TWENTY_DAYS_AGO = -20;
    private static final int TWENTY_DAYS_FROM_NOW = 20;

    @Inject
    private WebApplicationContext webApplicationContext;

    @Inject
    private VacancyRepository vacancyRepository;

    @Inject
    private DepartmentRepository departmentRepository;

    @MockBean
    private LocationService locationService;

    private MockMvc mvc;

    private Department department1;

    final private MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private Vacancy vacancy4 = Vacancy.builder()
            .identifier(398457344L)
            .title("testTitle")
            .description("testDescription")
            .grade("testGrade")
            .responsibilities("testResponsibilities")
            .workingHours("testWorkingHours")
            .closingDate(THIRTY_DAYS_FROM_NOW)
            .contactName("testContactName")
            .contactDepartment("testContactDepartment")
            .contactEmail("testContactEmail")
            .contactTelephone("testContactTelephone")
            .eligibility("testEligibility")
            .salaryMin(10)
            .salaryMax(100)
            .numberVacancies(1)
            .build();

    private Vacancy vacancy1 = Vacancy.builder()
            .id(1L)
            .identifier(398457345L)
            .title("testTile1 SearchQueryTitle")
            .description("testDescription1 SearchQueryDescription")
            .shortDescription("shortDescription")
            .grade("testGrade1 SearchQueryGrade")
            .responsibilities("testResponsibilities1")
            .workingHours("testWorkingHours1")
            .closingDate(THIRTY_DAYS_FROM_NOW)
            .contactName("testContactName1")
            .contactDepartment("testContactDepartment1")
            .contactEmail("testContactEmail1")
            .contactTelephone("testContactTelephone1")
            .eligibility("testEligibility1")
            .lengthOfEmployment("One Year")
            .salaryMin(0)
            .salaryMax(10)
            .numberVacancies(1)
            .displayCscContent(Boolean.TRUE)
            .selectionProcessDetails("selectionProcessDetails")
            .nationalityStatement(NationalityStatement.NON_RESERVED)
            .contractTypes(ContractType.FULL_TIME.toString())
            .workingPatterns(WorkingPattern.FLEXIBLE_WORKING.toString())
            .whatWeOffer("whatWeOffer")
            .locationOverride("locationOverride")
            .personalSpecification("personalSpecification")
            .build();

    {
        VacancyLocation vacancyLocation = VacancyLocation.builder()
                .latitude(BRISTOL_LATITUDE)
                .longitude(BRISTOL_LONGITUDE)
                .location("testLocation1 SearchQueryLocation")
                .build();
        vacancyLocation.setVacancy(vacancy1);
        vacancy1.getVacancyLocations().add(vacancyLocation);
    }

    private Vacancy vacancy2 = Vacancy.builder()
            .id(2L)
            .identifier(398457346L)
            .title("testTitle2")
            .description("testDescription2")
            .grade("testGrade2")
            .responsibilities("testResponsibilities2")
            .workingHours("testWorkingHours2")
            .closingDate(THIRTY_DAYS_FROM_NOW)
            .contactName("testContactName2")
            .contactDepartment("testContactDepartment2")
            .contactEmail("testContactEmail2")
            .contactTelephone("testContactTelephone2")
            .eligibility("testEligibility2")
            .salaryMin(0)
            .salaryMax(10)
            .numberVacancies(2)
            .build();

    private Vacancy vacancy3 = Vacancy.builder()
            .id(3L)
            .identifier(398457347L)
            .title("testTitle3")
            .description("testDescription3")
            .grade("testGrade3")
            .responsibilities("testResponsibilities3")
            .workingHours("testWorkingHours2")
            .closingDate(THIRTY_DAYS_FROM_NOW)
            .contactName("testContactName3")
            .contactDepartment("testContactDepartment3")
            .contactEmail("testContactEmail3")
            .contactTelephone("testContactTelephone3")
            .eligibility("testEligibility3")
            .salaryMin(0)
            .salaryMax(10)
            .numberVacancies(2)
            .build();

    @BeforeMethod
    void setup() throws LocationServiceException {

        this.vacancyRepository.deleteAll();
        this.departmentRepository.deleteAll();

        this.mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        department1 = departmentRepository.save(Department.builder().id(1L).name("Department One").build());
        Department department2 = departmentRepository.save(Department.builder().id(2L).name("Department Two").build());
        Department department3 = departmentRepository.save(Department.builder().id(3L).name("Department Three").build());

        vacancy1.setDepartment(department1);
        Vacancy savedVacancy1 = this.vacancyRepository.save(vacancy1);
        vacancy1.setId(savedVacancy1.getId());

        vacancy2.setDepartment(department2);
        Vacancy savedVacancy2 = this.vacancyRepository.save(vacancy2);
        vacancy2.setId(savedVacancy2.getId());

        vacancy3.setDepartment(department3);
        Vacancy savedVacancy3 = this.vacancyRepository.save(vacancy3);
        vacancy3.setId(savedVacancy3.getId());

        given(locationService.find(any()))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));
    }

    @AfterMethod
    void tearDown() {
    }

    @Test
    public void testDepartments_requestAllRows() throws Exception {

        String path = "/departments";
        ResultActions sendRequest = mvc.perform(get(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE")));
        sendRequest.andExpect(status().isNotFound());
    }

    @Test
    public void testVacancies_requestAllRows() throws Exception {

        String path = "/vacancies";
        ResultActions sendRequest = mvc.perform(get(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE")));
        sendRequest.andExpect(status().isNotFound());
    }

    @Test
    public void testFindAll() throws Exception {

        // Given
        String path = "/vacancy";

        // When
        ResultActions sendRequest = mvc.perform(get(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE")));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.content[0].id", is(toIntExact(this.vacancy1.getId()))))
                .andExpect(jsonPath("$.content[0].identifier", is(toIntExact(this.vacancy1.getIdentifier()))))
                .andExpect(jsonPath("$.content[0].description", is(this.vacancy1.getDescription())))
                .andExpect(jsonPath("$.content[0].grade", is(this.vacancy1.getGrade())))
                .andExpect(jsonPath("$.content[0].responsibilities", is(this.vacancy1.getResponsibilities())))
                .andExpect(jsonPath("$.content[0].workingHours", is(this.vacancy1.getWorkingHours())))
                .andExpect(jsonPath("$.content[0].closingDate", is(ISO_DATEFORMAT.format(vacancy4.getClosingDate()))))
                .andExpect(jsonPath("$.content[0].contactName", is(this.vacancy1.getContactName())))
                .andExpect(jsonPath("$.content[0].contactDepartment", is(this.vacancy1.getContactDepartment())))
                .andExpect(jsonPath("$.content[0].contactEmail", is(this.vacancy1.getContactEmail())))
                .andExpect(jsonPath("$.content[0].contactTelephone", is(this.vacancy1.getContactTelephone())))
                .andExpect(jsonPath("$.content[0].eligibility", is(this.vacancy1.getEligibility())))
                .andExpect(jsonPath("$.content[0].salaryMin", is(this.vacancy1.getSalaryMin())))
                .andExpect(jsonPath("$.content[0].salaryMax", is(this.vacancy1.getSalaryMax())))
                .andExpect(jsonPath("$.content[0].numberVacancies", is(this.vacancy1.getNumberVacancies())))
                .andExpect(jsonPath("$.content[0].lengthOfEmployment", is(this.vacancy1.getLengthOfEmployment())))
                .andExpect(jsonPath("$.content[1].id", is(toIntExact(this.vacancy2.getId()))))
                .andExpect(jsonPath("$.content[1].identifier", is(toIntExact(this.vacancy2.getIdentifier()))))
                .andExpect(jsonPath("$.content[1].description", is(this.vacancy2.getDescription())))
                .andExpect(jsonPath("$.content[1].grade", is(this.vacancy2.getGrade())))
                .andExpect(jsonPath("$.content[1].responsibilities", is(this.vacancy2.getResponsibilities())))
                .andExpect(jsonPath("$.content[1].workingHours", is(this.vacancy2.getWorkingHours())))
                .andExpect(jsonPath("$.content[1].closingDate", is(ISO_DATEFORMAT.format(vacancy4.getClosingDate()))))
                .andExpect(jsonPath("$.content[1].contactName", is(this.vacancy2.getContactName())))
                .andExpect(jsonPath("$.content[1].contactDepartment", is(this.vacancy2.getContactDepartment())))
                .andExpect(jsonPath("$.content[1].contactEmail", is(this.vacancy2.getContactEmail())))
                .andExpect(jsonPath("$.content[1].contactTelephone", is(this.vacancy2.getContactTelephone())))
                .andExpect(jsonPath("$.content[1].eligibility", is(this.vacancy2.getEligibility())))
                .andExpect(jsonPath("$.content[1].salaryMin", is(this.vacancy2.getSalaryMin())))
                .andExpect(jsonPath("$.content[1].salaryMax", is(this.vacancy2.getSalaryMax())))
                .andExpect(jsonPath("$.content[1].numberVacancies", is(this.vacancy2.getNumberVacancies())))
                .andExpect(jsonPath("$.content[2].id", is(toIntExact(this.vacancy3.getId()))))
                .andExpect(jsonPath("$.content[2].identifier", is(toIntExact(this.vacancy3.getIdentifier()))))
                .andExpect(jsonPath("$.content[2].description", is(this.vacancy3.getDescription())))
                .andExpect(jsonPath("$.content[2].grade", is(this.vacancy3.getGrade())))
                .andExpect(jsonPath("$.content[2].responsibilities", is(this.vacancy3.getResponsibilities())))
                .andExpect(jsonPath("$.content[2].workingHours", is(this.vacancy3.getWorkingHours())))
                .andExpect(jsonPath("$.content[2].closingDate", is(ISO_DATEFORMAT.format(vacancy4.getClosingDate()))))
                .andExpect(jsonPath("$.content[2].contactName", is(this.vacancy3.getContactName())))
                .andExpect(jsonPath("$.content[2].contactDepartment", is(this.vacancy3.getContactDepartment())))
                .andExpect(jsonPath("$.content[2].contactEmail", is(this.vacancy3.getContactEmail())))
                .andExpect(jsonPath("$.content[2].contactTelephone", is(this.vacancy3.getContactTelephone())))
                .andExpect(jsonPath("$.content[2].eligibility", is(this.vacancy3.getEligibility())))
                .andExpect(jsonPath("$.content[2].salaryMin", is(this.vacancy3.getSalaryMin())))
                .andExpect(jsonPath("$.content[2].salaryMax", is(this.vacancy3.getSalaryMax())))
                .andExpect(jsonPath("$.content[2].numberVacancies", is(this.vacancy3.getNumberVacancies())));
    }

    @Test
    public void testFindById() throws Exception {

        // Given
        String path = "/vacancy/" + vacancy1.getId();

        // When
        ResultActions sendRequest = mvc.perform(get(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE")));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.identifier", is(toIntExact(this.vacancy1.getIdentifier()))))
                .andExpect(jsonPath("$.description", is(this.vacancy1.getDescription())))
                .andExpect(jsonPath("$.shortDescription", is(this.vacancy1.getShortDescription())))
                .andExpect(jsonPath("$.displayCscContent", is(this.vacancy1.getDisplayCscContent())))
                .andExpect(jsonPath("$.selectionProcessDetails", is(this.vacancy1.getSelectionProcessDetails())))
                .andExpect(jsonPath("$.grade", is(this.vacancy1.getGrade())))
                .andExpect(jsonPath("$.responsibilities", is(this.vacancy1.getResponsibilities())))
                .andExpect(jsonPath("$.workingHours", is(this.vacancy1.getWorkingHours())))
                .andExpect(jsonPath("$.closingDate", is(ISO_DATEFORMAT.format(vacancy4.getClosingDate()))))
                .andExpect(jsonPath("$.contactName", is(this.vacancy1.getContactName())))
                .andExpect(jsonPath("$.contactDepartment", is(this.vacancy1.getContactDepartment())))
                .andExpect(jsonPath("$.contactEmail", is(this.vacancy1.getContactEmail())))
                .andExpect(jsonPath("$.contactTelephone", is(this.vacancy1.getContactTelephone())))
                .andExpect(jsonPath("$.eligibility", is(this.vacancy1.getEligibility())))
                .andExpect(jsonPath("$.salaryMin", is(this.vacancy1.getSalaryMin())))
                .andExpect(jsonPath("$.salaryMax", is(this.vacancy1.getSalaryMax())))
                .andExpect(jsonPath("$.workingPatterns", is(this.vacancy1.getWorkingPatterns())))
                .andExpect(jsonPath("$.whatWeOffer", is(this.vacancy1.getWhatWeOffer())))
                .andExpect(jsonPath("$.locationOverride", is(this.vacancy1.getLocationOverride())))
                .andExpect(jsonPath("$.contractTypes", is(this.vacancy1.getContractTypes())))
                .andExpect(jsonPath("$.personalSpecification", is(this.vacancy1.getPersonalSpecification())))
                .andExpect(jsonPath("$.numberVacancies", is(this.vacancy1.getNumberVacancies())));
    }

    @Test
    public void testFindByIdNotFound() throws Exception {

        // Given
        String path = "/vacancy/-1";

        // When
        ResultActions sendRequest = mvc.perform(get(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE")));

        // Then
        sendRequest.andExpect(status().isNotFound());
    }

    @Test
    public void testCreate() throws Exception {

        // Given
        String path = "/vacancy";

        Vacancy vacancy = createVacancyPrototype();
        vacancy.setApplyURL("www.google.com");
        vacancy.setDepartment(department1);

        ObjectMapper objectMapper = new ObjectMapper();

        // When
        ResultActions sendRequest = mvc.perform(post(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE"))
                .contentType(APPLICATION_JSON_UTF8).content(objectMapper.writeValueAsString(vacancy)));

        MvcResult sendRequestResult = sendRequest.andReturn();

        String returnedLocation = sendRequestResult.getResponse().getRedirectedUrl();

        // Then
        sendRequest
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));

        Long createdVacancyId = getResourceIdFromUrl(returnedLocation);

        Vacancy storedVacancy = vacancyRepository.findOne(createdVacancyId);

        assertTrue(storedVacancy.getTitle().equals("testTile1 SearchQueryTitle"));
        Assertions.assertThat(storedVacancy).isEqualToIgnoringGivenFields(vacancy, "id", "vacancyLocations", "applyURL", "department");
        assertTrue(storedVacancy.getActive());
        assertEquals("https://www.google.com", storedVacancy.getApplyURL());
    }

    @Test
    public void testCreateVacancyWithoutDepartment() throws Exception {

        // Given
        String path = "/vacancy";

        Vacancy vacancy = createVacancyPrototype();

        ObjectMapper objectMapper = new ObjectMapper();

        // When
        ResultActions sendRequest = mvc.perform(post(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE"))
                .contentType(APPLICATION_JSON_UTF8).content(objectMapper.writeValueAsString(vacancy)));

        sendRequest.andReturn();

        // Then
        sendRequest.andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {

        Vacancy vacancy = createVacancyPrototype();
        vacancy.setDepartment(department1);
        vacancy.setTitle("testUpdate");
        vacancy.setApplyURL("1234@me.com");

        vacancy.getVacancyLocations().get(0).setLocation("My New Location Name");

        ObjectMapper objectMapper = new ObjectMapper();

        // Given
        String path = "/vacancy/" + vacancy1.getId();

        // When
        ResultActions sendRequest = mvc.perform(put(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE"))
                .contentType(APPLICATION_JSON_UTF8).content(objectMapper.writeValueAsString(vacancy)));

        // Then
        sendRequest
                .andExpect(status().isOk());

        Optional<Vacancy> optionalVacancy = vacancyRepository.findById(vacancy1.getId());

        assertEquals("title", "testUpdate", optionalVacancy.get().getTitle());
        assertEquals("location name", "My New Location Name", optionalVacancy.get().getVacancyLocations().get(0).getLocation());
        assertEquals(null, optionalVacancy.get().getApplyURL());
    }

    @Test
    public void testUpdateNotFound() throws Exception {

        // Given
        String path = "/vacancy/-1";

        Vacancy vacancy = createVacancyPrototype();
        ObjectMapper objectMapper = new ObjectMapper();

        // When
        ResultActions sendRequest = mvc.perform(put(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE"))
                .contentType(APPLICATION_JSON_UTF8).content(objectMapper.writeValueAsString(vacancy)));

        // Then
        sendRequest.andExpect(status().isNotFound());
    }

    private long getResourceIdFromUrl(String locationUrl) {
        String[] parts = locationUrl.split("/");
        return Long.valueOf(parts[parts.length - 1]);
    }

    @Test
    public void testDelete() throws Exception {

        // Given
        String path = "/vacancy/" + vacancy1.getId();
        // When
        ResultActions sendRequest = mvc.perform(delete(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE")));

        Iterable<Vacancy> vacancies = vacancyRepository.findAll();

        // Then
        sendRequest.andExpect(status().isNoContent());
        Assertions.assertThat(vacancies).hasSize(2);
        Vacancy vacancy = vacancies.iterator().next();
        Set<Long> validIds = new HashSet<>();
        validIds.add(vacancy2.getId());
        validIds.add(vacancy3.getId());
        Assertions.assertThat(validIds).contains(vacancy.getId());
    }

    @Test
    public void testFindAllPaginated() throws Exception {
        // Given
        String path = "/vacancy/?page=0&size=1";

        // When
        ResultActions sendRequest = mvc.perform(get(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE")));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.size", is(1)))
                .andExpect(jsonPath("$.numberOfElements", is(1)));
    }

    @Test
    public void search_invalidHttpVerb() throws Exception {

        Vacancy vacancy = createVacancyPrototype();
        ObjectMapper objectMapper = new ObjectMapper();

        String path = "/vacancy/search?page=0&size=1";

        mvc.perform(get(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE"))
                .contentType(APPLICATION_JSON_UTF8).content(objectMapper.writeValueAsString(vacancy)))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void search_noSearchParametersSupplied() throws Exception {
        // Given
        String path = "/vacancy/search?page=0&size=1";

        mvc.perform(post(path)
                .with(user("searchusername").password("searchpassword").roles("CRUD_ROLE"))
                .contentType(APPLICATION_JSON_UTF8).content("")).andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void search_noResultsForlocationAndKeywordAndEmptyDepartment() throws Exception {
        // Given
        String path = "/vacancy/search?page=0&size=1";

        String requestBody = "{\n" +
                "  \"department\": [],\n" +
                "  \"keyword\": \"Dumpty\",\n" +
                "  \"location\": {\n" +
                "    \"place\": \"Humpty\",\n" +
                "    \"radius\": \"30\"\n" +
                "  }\n" +
                "}";

        ResultActions sendRequest = mvc.perform(post(path)
                .with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(APPLICATION_JSON_UTF8).content(requestBody));

        String content = sendRequest.andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.readValue(content, SearchResponsePage.class);
        Assertions.assertThat(content).contains("\"numberOfElements\":0");
    }

    private static Timestamp getTime(int numberOfDaysFromNow) {
        Date date = Date.from(LocalDateTime.now().plusDays(numberOfDaysFromNow).atZone(ZoneId.systemDefault()).toInstant());
        return new Timestamp(date.getTime());
    }

    @Test(enabled = false)
    public void search_onlyInternalSearchesAllowed() throws Exception {
        doClosedPublicSearchTests(TEN_DAYS_AGO, 10, 1);
    }

    private void doClosedPublicSearchTests(Integer internalDateNumDaysFromNow, Integer governmentDateNumDaysFromNow, Integer publicDateNumDaysFromNow) throws Exception {
        if (governmentDateNumDaysFromNow != null) {
            vacancy3.setGovernmentOpeningDate(getTime(governmentDateNumDaysFromNow));
        }

        if (internalDateNumDaysFromNow != null) {
            vacancy3.setInternalOpeningDate(getTime(internalDateNumDaysFromNow));
        }

        if (publicDateNumDaysFromNow != null) {
            vacancy3.setPublicOpeningDate(getTime(1));
        }

        this.vacancyRepository.save(vacancy3);

        // Given
        String path = "/vacancy/search?page=0&size=1";

        String requestBody = "{\n" +
                "  \"department\": [\n" +
                "    \"1\",\"2\"\n" +
                "  ],\n" +
                "  \"keyword\": \"search\",\n" +
                "  \"location\": {\n" +
                "    \"place\": \"testLocation\",\n" +
                "    \"radius\": \"30\"\n" +
                "  }\n" +
                "}";

        ResultActions sendRequest = mvc.perform(post(path).contentType(APPLICATION_JSON_UTF8).content(requestBody));

        // Then
        sendRequest
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test(enabled = false)
    public void search_internalAndGovtSearchesAllowed() throws Exception {
        doClosedPublicSearchTests(TWENTY_DAYS_AGO, TEN_DAYS_AGO, 1);
        vacancy3.setGovernmentOpeningDate(getTime(TEN_DAYS_AGO));
        vacancy3.setInternalOpeningDate(getTime(TWENTY_DAYS_AGO));
        vacancy3.setPublicOpeningDate(getTime(TWENTY_DAYS_FROM_NOW));
        this.vacancyRepository.save(vacancy3);

        // Given
        String path = "/vacancy/search?page=0&size=1";

        String requestBody = "{\n" +
                "  \"department\": [\n" +
                "    \"1\",\"2\"\n" +
                "  ],\n" +
                "  \"keyword\": \"search\",\n" +
                "  \"location\": {\n" +
                "    \"place\": \"testLocation\",\n" +
                "    \"radius\": \"30\"\n" +
                "  }\n" +
                "}";

        ResultActions sendRequest = mvc.perform(post(path).contentType(APPLICATION_JSON_UTF8).content(requestBody));

        // Then
        sendRequest
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test(enabled = false)
    public void search_noOpeningDatesSet() throws Exception {
        doClosedPublicSearchTests(null, null, null);
        vacancy3.setGovernmentOpeningDate(getTime(TEN_DAYS_AGO));
        vacancy3.setInternalOpeningDate(getTime(TWENTY_DAYS_AGO));
        vacancy3.setPublicOpeningDate(getTime(TWENTY_DAYS_FROM_NOW));
        this.vacancyRepository.save(vacancy3);

        // Given
        String path = "/vacancy/search?page=0&size=1";

        String requestBody = "{\n" +
                "  \"department\": [\n" +
                "    \"1\",\"2\"\n" +
                "  ],\n" +
                "  \"keyword\": \"search\",\n" +
                "  \"location\": {\n" +
                "    \"place\": \"testLocation\",\n" +
                "    \"radius\": \"30\"\n" +
                "  }\n" +
                "}";

        ResultActions sendRequest = mvc.perform(post(path).contentType(APPLICATION_JSON_UTF8).content(requestBody));

        // Then
        sendRequest
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    private Vacancy createVacancyPrototype() {

        VacancyLocation vacancyLocation = VacancyLocation.builder()
                .latitude(BRISTOL_LATITUDE)
                .longitude(BRISTOL_LONGITUDE)
                .location("testLocation1 SearchQueryLocation")
                .build();

        Vacancy vacancy = Vacancy.builder()
                .department(null)
                .title("testTile1 SearchQueryTitle")
                .description("testDescription1 SearchQueryDescription")
                .grade("testGrade1 SearchQueryGrade")
                .responsibilities("testResponsibilities1")
                .workingHours("testWorkingHours1")
                .closingDate(THIRTY_DAYS_FROM_NOW)
                .publicOpeningDate(THIRTY_DAYS_FROM_NOW)
                .contactName("testContactName1")
                .contactDepartment("testContactDepartment1")
                .contactEmail("testContactEmail1")
                .contactTelephone("testContactTelephone1")
                .eligibility("testEligibility1")
                .salaryMin(0)
                .salaryMax(10)
                .numberVacancies(1)
                .identifier(System.currentTimeMillis())
                .build();

        vacancy.getVacancyLocations().add(vacancyLocation);

        return vacancy;
    }


 }
