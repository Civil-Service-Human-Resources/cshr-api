package uk.gov.cshr.vcm.controller;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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


    private Vacancy expectedVacancy = Vacancy.builder()
            .title("testVacancy")
            .description("testDescription")
            .location("testLocation")
            .grade("testGrade")
            .closingDate("testDate")
            .salaryMin(10)
            .salaryMax(100)
            .numberVacancies(1)
            .build();

    private Vacancy vacancy1 = Vacancy.builder()
            .id(1L)
            .title("testTile1 SearchQueryTitle")
            .description("testDescription1 SearchQueryDescription")
            .location("testLocation1 SearchQueryLocation")
            .grade("testGrade1 SearchQueryGrade")
            .closingDate("testClosingDate1 SearchQueryGrade")
            .salaryMin(0)
            .salaryMax(10)
            .numberVacancies(1)
            .build();

    private Vacancy vacancy2 = Vacancy.builder()
            .id(2L)
            .title("testTitle2")
            .description("testDescription2")
            .location("testLocation2")
            .grade("testGrade2")
            .closingDate("testGrade2")
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
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(toIntExact(this.vacancy1.getId()))))
                .andExpect(jsonPath("$[0].description", is(this.vacancy1.getDescription())))
                .andExpect(jsonPath("$[0].location", is(this.vacancy1.getLocation())))
                .andExpect(jsonPath("$[0].grade", is(this.vacancy1.getGrade())))
                .andExpect(jsonPath("$[0].closingDate", is(this.vacancy1.getClosingDate())))
                .andExpect(jsonPath("$[0].salaryMin", is(this.vacancy1.getSalaryMin())))
                .andExpect(jsonPath("$[0].salaryMax", is(this.vacancy1.getSalaryMax())))
                .andExpect(jsonPath("$[0].numberVacancies", is(this.vacancy1.getNumberVacancies())))
                .andExpect(jsonPath("$[1].id", is(toIntExact(this.vacancy2.getId()))))
                .andExpect(jsonPath("$[1].description", is(this.vacancy2.getDescription())))
                .andExpect(jsonPath("$[1].location", is(this.vacancy2.getLocation())))
                .andExpect(jsonPath("$[1].grade", is(this.vacancy2.getGrade())))
                .andExpect(jsonPath("$[1].closingDate", is(this.vacancy2.getClosingDate())))
                .andExpect(jsonPath("$[1].salaryMin", is(this.vacancy2.getSalaryMin())))
                .andExpect(jsonPath("$[1].salaryMax", is(this.vacancy2.getSalaryMax())))
                .andExpect(jsonPath("$[1].numberVacancies", is(this.vacancy2.getNumberVacancies())));

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

        String requestBody = "{" +
                "\"title\":\"" + expectedVacancy.getTitle() + "\"," +
                "\"description\":\"" + expectedVacancy.getDescription() + "\"," +
                "\"location\":\"" + expectedVacancy.getLocation() + "\"," +
                "\"grade\":\"" + expectedVacancy.getGrade() + "\"," +
                "\"closingDate\":\"" + expectedVacancy.getClosingDate() + "\"," +
                "\"salaryMin\":\"" + expectedVacancy.getSalaryMin() + "\"," +
                "\"salaryMax\":\"" + expectedVacancy.getSalaryMax() + "\"," +
                "\"numberVacancies\":\"" + expectedVacancy.getNumberVacancies() + "\"" +
                "}";

        // When
        ResultActions sendRequest = mvc.perform(post(path).contentType(APPLICATION_JSON_UTF8).content(requestBody));

        MvcResult sendRequestResult = sendRequest.andReturn();

        String returnedLocation = sendRequestResult.getResponse().getRedirectedUrl();

        Long createdVacancyId = getResourceIdFromUrl(returnedLocation);

        Vacancy storedVacancy = vacancyRepository.findOne(createdVacancyId);

        // Then
        sendRequest
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));

        Assertions.assertThat(storedVacancy).isEqualToIgnoringGivenFields(expectedVacancy, "id");
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

    @Test
    public void testSearchTitle() throws Exception {
        // Given
        String path = "/vacancy/search/location/searchQueryLocation/keyword/search";

        // When
        ResultActions sendRequest = mvc.perform(get(path));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(toIntExact(this.vacancy1.getId()))))
                .andExpect(jsonPath("$[0].description", is(this.vacancy1.getDescription())))
                .andExpect(jsonPath("$[0].location", is(this.vacancy1.getLocation())))
                .andExpect(jsonPath("$[0].salaryMin", is(this.vacancy1.getSalaryMin())))
                .andExpect(jsonPath("$[0].salaryMax", is(this.vacancy1.getSalaryMax())));

    }

}