package uk.gov.cshr.vcm.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.controller.exception.VacancyClosedException;
import uk.gov.cshr.vcm.controller.exception.VacancyError;
import uk.gov.cshr.vcm.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.model.fixture.VacancyFixture;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class ClosingDateTest extends SearchTestConfiguration {

    private static final Timestamp YESTERDAY = getTime(-1);
    private static final Timestamp TODAY = getTime(0);
    private static final Timestamp TOMORROW = getTime(+1);
    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30);
    private static final Timestamp THIRTY_DAYS_AGO = getTime(-30);

    private Department department;

    @Before
    public void before() throws LocationServiceException {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        department = departmentRepository.save(
                Department.builder()
                        .name("Department One")
                        .disabilityLogo("disabilityLogo")
                        .build());

        initLocationService();
    }

    @Test
    public void testExcludeClosedVacancies() throws Exception {

        createVacancyWithClosingDate(null, YESTERDAY, department);

        // because these are timestamp based this job will be closed
        createVacancyWithClosingDate(12346L, TODAY, department);
        createVacancyWithClosingDate(12347L, TOMORROW, department);
        createVacancyWithClosingDate(12348L, THIRTY_DAYS_FROM_NOW, department);

        Page<Vacancy> result = findVancancies();
        List<Vacancy> resultsList = result.getContent();

        Assert.assertEquals("Expected results", 2, resultsList.size());

        resultsList.stream().filter((vacancy) -> (vacancy.getClosingDate().compareTo(new Date()) < 0))
                .forEachOrdered((vacancy) -> fail("vacancy.getClosingDate() in past: " + vacancy.getClosingDate()));
    }

    @Test
    public void testGetClosedVacancy() throws Exception {

        Vacancy closedVacancy = createVacancyWithClosingDate(null, YESTERDAY, department);

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

        Vacancy closedVacancy = createVacancyWithClosingDate(null, YESTERDAY, department);
        closedVacancy.setClosingDate(THIRTY_DAYS_FROM_NOW);

        this.mockMvc.perform(put("/vacancy/" + closedVacancy.getId())
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

    private Page<Vacancy> findVancancies() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
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

    private Vacancy createVacancyWithClosingDate(Long identifier, Timestamp closingDate, Department department) {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();

        if (identifier != null) {
            vacancy.setIdentifier(identifier);
        }

        vacancy.setDepartment(department);
        vacancy.setClosingDate(closingDate);
        vacancy.setLatitude(BRISTOL.getLatitude());
        vacancy.setLongitude(BRISTOL.getLongitude());
        vacancy.setPublicOpeningDate(THIRTY_DAYS_AGO);

        return vacancyRepository.save(vacancy);
    }
}
