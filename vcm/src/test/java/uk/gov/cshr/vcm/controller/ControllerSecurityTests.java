package uk.gov.cshr.vcm.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyLocation;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.LocationService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class ControllerSecurityTests extends AbstractTestNGSpringContextTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private VacancyRepository vacancyRepository;

    @MockBean
    private LocationService locationService;

    @BeforeMethod
    public void setup() throws LocationServiceException {

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        Vacancy savedVacancy = createVacancyPrototype();
        savedVacancy.setId(Long.MIN_VALUE);
        when(vacancyRepository.save(any(Vacancy.class))).thenReturn(savedVacancy);

        given(locationService.find(any()))
                .willReturn(new Coordinates(Double.MIN_VALUE, Double.MIN_VALUE, "South West"));
    }

    @Test
    public void testVacancySearch() throws Exception {

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("SearchQueryDescription")
                .overseasJob(Boolean.TRUE)
                .location(new Location("anywhere", 30))
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(vacancySearchParameters);

        this.mockMvc.perform(post("/vacancy/search")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isUnauthorized())
                .andReturn();

        this.mockMvc.perform(post("/vacancy/search")
                .with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testVacancyControllerCreate() throws Exception {

        Vacancy vacancy = createVacancyPrototype();
        String jsonVacancy = new ObjectMapper().writeValueAsString(vacancy);

        mockMvc.perform(post("/vacancy")
                .with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonVacancy)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isForbidden())
                .andReturn();

        mockMvc.perform(post("/vacancy")
                .with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonVacancy)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isForbidden())
                .andReturn();

        mockMvc.perform(post("/vacancy")
                .with(user("crudusername").password("crudpassword").roles("CRUD_ROLE"))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonVacancy)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    public void testGetDepartment() throws Exception {

        mockMvc.perform(get("/department")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isUnauthorized())
                .andReturn();

        mockMvc.perform(get("/department")
                .with(user("crudusername").password("crudpassword").roles("CRUD_ROLE"))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get("/department")
                .with(user("crudusername").password("crudpassword").roles("SEARCH_ROLE"))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();
    }

    private Vacancy createVacancyPrototype() {

        VacancyLocation vacancyLocation = VacancyLocation.builder()
                .latitude(Double.MIN_VALUE)
                .longitude(Double.MIN_VALUE)
                .location("testLocation1 SearchQueryLocation")
                .build();

        Department department = Department.builder()
                .name("Department One")
                .disabilityLogo("disabilityLogo")
                .build();

        Vacancy vacancy = Vacancy.builder()
                .department(department)
                .title("testTile1 SearchQueryTitle")
                .description("testDescription1 SearchQueryDescription")
                .grade("testGrade1 SearchQueryGrade")
                .responsibilities("testResponsibilities1")
                .workingHours("testWorkingHours1")
                .closingDate(new Timestamp(0l))
                .publicOpeningDate(new Date())
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

        vacancy.setVacancyLocations(new ArrayList<>());
        vacancy.getVacancyLocations().add(vacancyLocation);
        return vacancy;
    }
}