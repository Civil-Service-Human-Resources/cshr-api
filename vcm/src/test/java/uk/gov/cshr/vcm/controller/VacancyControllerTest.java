package uk.gov.cshr.vcm.controller;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.repository.VacancyRepository;


import java.nio.charset.Charset;

import static java.lang.Math.toIntExact;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
public class VacancyControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private VacancyRepository vacancyRepository;

    private MockMvc mvc;

    final private MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));


    private Vacancy requestBodyVacancy = Vacancy.builder()
            .title("testTitle")
            .description("testDescription")
            .location("testLocation")
            .grade("testGrade")
            .role("testRole")
            .responsibilities("testResponsibilities")
            .workingHours("testWorkingHours")
            .closingDate("testClosingDate")
            .contactName("testContactName")
            .contactDepartment("testContactDepartment")
            .contactEmail("testContactEmail")
            .contactTelephone("testContactTelephone")
            .eligibility("testEligibility")
            .salaryMin(10)
            .salaryMax(100)
            .numberVacancies(1)
            .build();



    private String requestBody = "{" +
            "\"title\":\"" + requestBodyVacancy.getTitle() + "\"," +
            "\"description\":\"" + requestBodyVacancy.getDescription() + "\"," +
            "\"location\":\"" + requestBodyVacancy.getLocation() + "\"," +
            "\"grade\":\"" + requestBodyVacancy.getGrade() + "\"," +
            "\"role\":\"" + requestBodyVacancy.getRole() + "\"," +
            "\"responsibilities\":\"" + requestBodyVacancy.getResponsibilities() + "\"," +
            "\"workingHours\":\"" + requestBodyVacancy.getWorkingHours() + "\"," +
            "\"closingDate\":\"" + requestBodyVacancy.getClosingDate() + "\"," +
            "\"contactName\":\"" + requestBodyVacancy.getContactName() + "\"," +
            "\"contactDepartment\":\"" + requestBodyVacancy.getContactDepartment() + "\"," +
            "\"contactEmail\":\"" + requestBodyVacancy.getContactEmail() + "\"," +
            "\"contactTelephone\":\"" + requestBodyVacancy.getContactTelephone() + "\"," +
            "\"eligibility\":\"" + requestBodyVacancy.getEligibility() + "\"," +
            "\"salaryMin\":" + requestBodyVacancy.getSalaryMin() + "," +
            "\"salaryMax\":" + requestBodyVacancy.getSalaryMax() + "," +
            "\"numberVacancies\":" + requestBodyVacancy.getNumberVacancies() + "" +
            "}";

    private Vacancy vacancy1 = Vacancy.builder()
            .id(1)
            .title("testTile1 SearchQueryTitle")
            .description("testDescription1 SearchQueryDescription")
            .location("testLocation1 SearchQueryLocation")
            .grade("testGrade1 SearchQueryGrade")
            .role("testRole1")
            .responsibilities("testResponsibilities1")
            .workingHours("testWorkingHours1")
            .closingDate("testClosingDate1 SearchQueryGrade")
            .contactName("testContactName1")
            .contactDepartment("testContactDepartment1")
            .contactEmail("testContactEmail1")
            .contactTelephone("testContactTelephone1")
            .eligibility("testEligibility1")
            .salaryMin(0)
            .salaryMax(10)
            .numberVacancies(1)
            .build();

    private Vacancy vacancy2 = Vacancy.builder()
            .id(2)
            .title("testTitle2")
            .description("testDescription2")
            .location("testLocation2")
            .grade("testGrade2")
            .role("testRole2")
            .responsibilities("testResponsibilities2")
            .workingHours("testWorkingHours2")
            .closingDate("testClosingDate2")
            .contactName("testContactName2")
            .contactDepartment("testContactDepartment2")
            .contactEmail("testContactEmail2")
            .contactTelephone("testContactTelephone2")
            .eligibility("testEligibility2")
            .salaryMin(0)
            .salaryMax(10)
            .numberVacancies(2)
            .build();

    @BeforeMethod
    void setup() {

        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        this.vacancyRepository.deleteAll();

        Vacancy savedVacancy1 = this.vacancyRepository.save(vacancy1);
        vacancy1.setId(savedVacancy1.getId());

        Vacancy savedVacancy2 = this.vacancyRepository.save(vacancy2);
        vacancy2.setId(savedVacancy2.getId());

    }

    @Test
    public void testFindAll() throws Exception {
        // Given
        String path = "/vacancy";

        // When
        ResultActions sendRequest = mvc.perform(get(path));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].id", is(toIntExact(this.vacancy1.getId()))))
                .andExpect(jsonPath("$.content[0].description", is(this.vacancy1.getDescription())))
                .andExpect(jsonPath("$.content[0].location", is(this.vacancy1.getLocation())))
                .andExpect(jsonPath("$.content[0].grade", is(this.vacancy1.getGrade())))
                .andExpect(jsonPath("$.content[0].role", is(this.vacancy1.getRole())))
                .andExpect(jsonPath("$.content[0].responsibilities", is(this.vacancy1.getResponsibilities())))
                .andExpect(jsonPath("$.content[0].workingHours", is(this.vacancy1.getWorkingHours())))
                .andExpect(jsonPath("$.content[0].closingDate", is(this.vacancy1.getClosingDate())))
                .andExpect(jsonPath("$.content[0].contactName", is(this.vacancy1.getContactName())))
                .andExpect(jsonPath("$.content[0].contactDepartment", is(this.vacancy1.getContactDepartment())))
                .andExpect(jsonPath("$.content[0].contactEmail", is(this.vacancy1.getContactEmail())))
                .andExpect(jsonPath("$.content[0].contactTelephone", is(this.vacancy1.getContactTelephone())))
                .andExpect(jsonPath("$.content[0].eligibility", is(this.vacancy1.getEligibility())))
                .andExpect(jsonPath("$.content[0].salaryMin", is(this.vacancy1.getSalaryMin())))
                .andExpect(jsonPath("$.content[0].salaryMax", is(this.vacancy1.getSalaryMax())))
                .andExpect(jsonPath("$.content[0].numberVacancies", is(this.vacancy1.getNumberVacancies())))
                .andExpect(jsonPath("$.content[1].id", is(toIntExact(this.vacancy2.getId()))))
                .andExpect(jsonPath("$.content[1].description", is(this.vacancy2.getDescription())))
                .andExpect(jsonPath("$.content[1].location", is(this.vacancy2.getLocation())))
                .andExpect(jsonPath("$.content[1].grade", is(this.vacancy2.getGrade())))
                .andExpect(jsonPath("$.content[1].role", is(this.vacancy2.getRole())))
                .andExpect(jsonPath("$.content[1].responsibilities", is(this.vacancy2.getResponsibilities())))
                .andExpect(jsonPath("$.content[1].workingHours", is(this.vacancy2.getWorkingHours())))
                .andExpect(jsonPath("$.content[1].closingDate", is(this.vacancy2.getClosingDate())))
                .andExpect(jsonPath("$.content[1].contactName", is(this.vacancy2.getContactName())))
                .andExpect(jsonPath("$.content[1].contactDepartment", is(this.vacancy2.getContactDepartment())))
                .andExpect(jsonPath("$.content[1].contactEmail", is(this.vacancy2.getContactEmail())))
                .andExpect(jsonPath("$.content[1].contactTelephone", is(this.vacancy2.getContactTelephone())))
                .andExpect(jsonPath("$.content[1].eligibility", is(this.vacancy2.getEligibility())))
                .andExpect(jsonPath("$.content[1].salaryMin", is(this.vacancy2.getSalaryMin())))
                .andExpect(jsonPath("$.content[1].salaryMax", is(this.vacancy2.getSalaryMax())))
                .andExpect(jsonPath("$.content[1].numberVacancies", is(this.vacancy2.getNumberVacancies())));

    }

    @Test
    public void testFindById() throws Exception {
        // Given
        String path = "/vacancy/" + vacancy1.getId();

        // When
        ResultActions sendRequest = mvc.perform(get(path));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.description", is(this.vacancy1.getDescription())))
                .andExpect(jsonPath("$.location", is(this.vacancy1.getLocation())))
                .andExpect(jsonPath("$.grade", is(this.vacancy1.getGrade())))
                .andExpect(jsonPath("$.role", is(this.vacancy1.getRole())))
                .andExpect(jsonPath("$.responsibilities", is(this.vacancy1.getResponsibilities())))
                .andExpect(jsonPath("$.workingHours", is(this.vacancy1.getWorkingHours())))
                .andExpect(jsonPath("$.closingDate", is(this.vacancy1.getClosingDate())))
                .andExpect(jsonPath("$.contactName", is(this.vacancy1.getContactName())))
                .andExpect(jsonPath("$.contactDepartment", is(this.vacancy1.getContactDepartment())))
                .andExpect(jsonPath("$.contactEmail", is(this.vacancy1.getContactEmail())))
                .andExpect(jsonPath("$.contactTelephone", is(this.vacancy1.getContactTelephone())))
                .andExpect(jsonPath("$.eligibility", is(this.vacancy1.getEligibility())))
                .andExpect(jsonPath("$.salaryMin", is(this.vacancy1.getSalaryMin())))
                .andExpect(jsonPath("$.salaryMax", is(this.vacancy1.getSalaryMax())))
                .andExpect(jsonPath("$.numberVacancies", is(this.vacancy1.getNumberVacancies())));

    }

    @Test
    public void testFindByIdNotFound() throws Exception {
        // Given
        String path = "/vacancy/-1";

        // When
        ResultActions sendRequest = mvc.perform(get(path));

        // Then
        sendRequest.andExpect(status().isNotFound());

    }

    @Test
    public void testCreate() throws Exception {
        // Given
        String path = "/vacancy";

        // When
        ResultActions sendRequest = mvc.perform(post(path).contentType(APPLICATION_JSON_UTF8).content(requestBody));

        MvcResult sendRequestResult = sendRequest.andReturn();

        String returnedLocation = sendRequestResult.getResponse().getRedirectedUrl();

        // Then
        sendRequest
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));

        Long createdVacancyId = getResourceIdFromUrl(returnedLocation);

        Vacancy storedVacancy = vacancyRepository.findOne(createdVacancyId);

        Assertions.assertThat(storedVacancy).isEqualToIgnoringGivenFields(requestBodyVacancy, "id");
    }


    @Test
    public void testUpdate() throws Exception {
        // Given
        String path = "/vacancy/" + vacancy1.getId();

        // When
        ResultActions sendRequest = mvc.perform(put(path).contentType(APPLICATION_JSON_UTF8).content(requestBody));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(toIntExact(this.vacancy1.getId()))))
                .andExpect(jsonPath("$.description", is(this.requestBodyVacancy.getDescription())))
                .andExpect(jsonPath("$.location", is(this.requestBodyVacancy.getLocation())))
                .andExpect(jsonPath("$.grade", is(this.requestBodyVacancy.getGrade())))
                .andExpect(jsonPath("$.role", is(this.requestBodyVacancy.getRole())))
                .andExpect(jsonPath("$.responsibilities", is(this.requestBodyVacancy.getResponsibilities())))
                .andExpect(jsonPath("$.workingHours", is(this.requestBodyVacancy.getWorkingHours())))
                .andExpect(jsonPath("$.closingDate", is(this.requestBodyVacancy.getClosingDate())))
                .andExpect(jsonPath("$.contactName", is(this.requestBodyVacancy.getContactName())))
                .andExpect(jsonPath("$.contactDepartment", is(this.requestBodyVacancy.getContactDepartment())))
                .andExpect(jsonPath("$.contactEmail", is(this.requestBodyVacancy.getContactEmail())))
                .andExpect(jsonPath("$.contactTelephone", is(this.requestBodyVacancy.getContactTelephone())))
                .andExpect(jsonPath("$.eligibility", is(this.requestBodyVacancy.getEligibility())))
                .andExpect(jsonPath("$.salaryMin", is(this.requestBodyVacancy.getSalaryMin())))
                .andExpect(jsonPath("$.salaryMax", is(this.requestBodyVacancy.getSalaryMax())))
                .andExpect(jsonPath("$.numberVacancies", is(this.requestBodyVacancy.getNumberVacancies())));

    }

    @Test
    public void testUpdateNotFound() throws Exception {
        // Given
        String path = "/vacancy/-1";

        // When
        ResultActions sendRequest = mvc.perform(put(path).contentType(APPLICATION_JSON_UTF8).content(requestBody));

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
        ResultActions sendRequest = mvc.perform(delete(path));

        Iterable<Vacancy> vacancies = vacancyRepository.findAll();

        // Then
        sendRequest.andExpect(status().isNoContent());
        Assertions.assertThat(vacancies).hasSize(1);
        Assertions.assertThat(vacancies.iterator().next()).isEqualToComparingFieldByField(vacancy2);

    }


    /**
     * Current search implementation is primitive, this test will evolve with time
     * @throws Exception
     */
    @Test
    public void testSearch() throws Exception {
        // Given
        String path = "/vacancy/search/location/searchQueryLocation/keyword/search?page=0&size=1";

        // When
        ResultActions sendRequest = mvc.perform(get(path));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].id", is(toIntExact(this.vacancy1.getId()))))
                .andExpect(jsonPath("$.content[0].description", is(this.vacancy1.getDescription())))
                .andExpect(jsonPath("$.content[0].location", is(this.vacancy1.getLocation())))
                .andExpect(jsonPath("$.content[0].grade", is(this.vacancy1.getGrade())))
                .andExpect(jsonPath("$.content[0].role", is(this.vacancy1.getRole())))
                .andExpect(jsonPath("$.content[0].responsibilities", is(this.vacancy1.getResponsibilities())))
                .andExpect(jsonPath("$.content[0].workingHours", is(this.vacancy1.getWorkingHours())))
                .andExpect(jsonPath("$.content[0].closingDate", is(this.vacancy1.getClosingDate())))
                .andExpect(jsonPath("$.content[0].contactName", is(this.vacancy1.getContactName())))
                .andExpect(jsonPath("$.content[0].contactDepartment", is(this.vacancy1.getContactDepartment())))
                .andExpect(jsonPath("$.content[0].contactEmail", is(this.vacancy1.getContactEmail())))
                .andExpect(jsonPath("$.content[0].contactTelephone", is(this.vacancy1.getContactTelephone())))
                .andExpect(jsonPath("$.content[0].eligibility", is(this.vacancy1.getEligibility())))
                .andExpect(jsonPath("$.content[0].salaryMin", is(this.vacancy1.getSalaryMin())))
                .andExpect(jsonPath("$.content[0].salaryMax", is(this.vacancy1.getSalaryMax())))
                .andExpect(jsonPath("$.content[0].numberVacancies", is(this.vacancy1.getNumberVacancies())));

    }

    @Test
    public void testFindAllPaginated() throws Exception {
        // Given
        String path = "/vacancy/?page=0&size=1";

        // When
        ResultActions sendRequest = mvc.perform(get(path));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.size", is(1)))
                .andExpect(jsonPath("$.numberOfElements", is(1)));

    }

}