package uk.gov.cshr.vcm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.assertj.core.api.Fail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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

    private Department department;

    @Before
    public void before() {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        department = departmentRepository.save(Department.builder().name("Department One").build());
        createdDepartments.add(department);

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
    public void testFindMinSalaryNoMax() throws Exception {

        createVacancyWithSalaryRange(14000, null, department);

        Page<Vacancy> result = findVancancies(10000, 20000);
        List<Vacancy> resultsList = result.getContent();

        boolean vacancyFound = false;

        for (Vacancy vacancy : resultsList) {

            if (vacancy.getSalaryMin().equals(14000)) {
                vacancyFound = true;
            }
        }

        Assert.assertTrue(vacancyFound);
    }

    @Test
    public void testFindMinSalaryWithMinAndMax() throws Exception {

        createVacancyWithSalaryRange(14000, 48000, department);
        createVacancyWithSalaryRange(20000, 29000, department);
        createVacancyWithSalaryRange(41000, 50000, department);

        Page<Vacancy> result = findVancancies(30000, 40000);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());

        boolean vacancyFound = false;

        for (Vacancy vacancy : resultsList) {

            if (vacancy.getSalaryMin().equals(14000)) {
                vacancyFound = true;
            }
            else {
                Fail.fail("Vacancy with salary range " + vacancy.getSalaryMin() + "-" + vacancy.getSalaryMax() + "  when filtering for 30000-40000");
            }
        }

        Assert.assertTrue(vacancyFound);
    }

    @Test
    public void testSalaries1() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(10000, null);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.contains(v1));
        Assert.assertTrue(resultsList.contains(v2));
        Assert.assertTrue(resultsList.contains(v3));
        Assert.assertTrue(resultsList.contains(v4));
        Assert.assertTrue(resultsList.contains(v5));
    }

    @Test
    public void testSalaries2() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(40000, null);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertFalse(resultsList.contains(v1));
        Assert.assertTrue(resultsList.contains(v2));
        Assert.assertTrue(resultsList.contains(v3));
        Assert.assertTrue(resultsList.contains(v4));
        Assert.assertTrue(resultsList.contains(v5));
    }

    @Test
    public void testSalaries3() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(50000, null);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertFalse(resultsList.contains(v1));
        Assert.assertTrue(resultsList.contains(v2));
        Assert.assertTrue(resultsList.contains(v3));
        Assert.assertTrue(resultsList.contains(v4));
        Assert.assertTrue(resultsList.contains(v5));
    }

    @Test
    public void testSalaries4() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(60000, null);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertFalse(resultsList.contains(v1));
        Assert.assertFalse(resultsList.contains(v2));
        Assert.assertTrue(resultsList.contains(v3));
        Assert.assertTrue(resultsList.contains(v4));
        Assert.assertTrue(resultsList.contains(v5));
    }

    @Test
    public void testSalaries5() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(70000, null);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertFalse(resultsList.contains(v1));
        Assert.assertFalse(resultsList.contains(v2));
        Assert.assertFalse(resultsList.contains(v3));
        Assert.assertFalse(resultsList.contains(v4));
        Assert.assertFalse(resultsList.contains(v5));
    }

    @Test
    public void testSalaries6() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(10000, 30000);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.contains(v1));
        Assert.assertFalse(resultsList.contains(v2));
        Assert.assertFalse(resultsList.contains(v3));
        Assert.assertFalse(resultsList.contains(v4));
        Assert.assertFalse(resultsList.contains(v5));
    }

    @Test
    public void testSalaries7() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(30000, 40000);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.contains(v1));
        Assert.assertFalse(resultsList.contains(v2));
        Assert.assertFalse(resultsList.contains(v3));
        Assert.assertFalse(resultsList.contains(v4));
        Assert.assertFalse(resultsList.contains(v5));
    }

    @Test
    public void testSalaries8() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(30000, 50000);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.contains(v1));
        Assert.assertTrue(resultsList.contains(v2));
        Assert.assertTrue(resultsList.contains(v3));
        Assert.assertFalse(resultsList.contains(v4));
        Assert.assertFalse(resultsList.contains(v5));
    }

    @Test
    public void testSalaries9() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(40000, 50000);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertFalse(resultsList.contains(v1));
        Assert.assertTrue(resultsList.contains(v2));
        Assert.assertTrue(resultsList.contains(v3));
        Assert.assertFalse(resultsList.contains(v4));
        Assert.assertFalse(resultsList.contains(v5));
    }

    @Test
    public void testSalaries10() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(50000, 60000);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertFalse(resultsList.contains(v1));
        Assert.assertTrue(resultsList.contains(v2));
        Assert.assertTrue(resultsList.contains(v3));
        Assert.assertTrue(resultsList.contains(v4));
        Assert.assertTrue(resultsList.contains(v5));
    }

    @Test
    public void testSalaries11() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(60000, 60000);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertFalse(resultsList.contains(v1));
        Assert.assertFalse(resultsList.contains(v2));
        Assert.assertTrue(resultsList.contains(v3));
        Assert.assertTrue(resultsList.contains(v4));
        Assert.assertTrue(resultsList.contains(v5));
    }

    @Test
    public void testSalaries12() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department);

        Page<Vacancy> result = findVancancies(70000, 70000);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertFalse(resultsList.contains(v1));
        Assert.assertFalse(resultsList.contains(v2));
        Assert.assertFalse(resultsList.contains(v3));
        Assert.assertFalse(resultsList.contains(v4));
        Assert.assertFalse(resultsList.contains(v5));
    }

    @Test
    public void testMaxSalary() throws Exception {

        createVacancyWithSalaryRange(40000, 50000, department);
        createVacancyWithSalaryRange(69001, 70000, department);
        createVacancyWithSalaryRange(69000, 70000, department);
        createVacancyWithSalaryRange(40000, 69000, department);

        Page<Vacancy> result = findVancancies(null, 69000);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());

        for (Vacancy vacancy : resultsList) {

            if (vacancy.getSalaryMin().compareTo(69001) == 0) {
                Fail.fail("Vacancy with min salary of " + vacancy.getSalaryMin() + " when filtering for max of 69000");
            }
        }
    }

    @Test
    public void testMinSalary() throws Exception {

        createVacancyWithSalaryRange(10000, null, department);
        createVacancyWithSalaryRange(10000, 20000, department);
        createVacancyWithSalaryRange(20000, 30000, department);
        createVacancyWithSalaryRange(30000, 40000, department);
        createVacancyWithSalaryRange(40000, 50000, department);

        Vacancy v1 = createVacancyWithSalaryRange(60000, 70000, department);
        Vacancy v2 = createVacancyWithSalaryRange(70000, 70001, department);
        Vacancy v3 = createVacancyWithSalaryRange(70001, 80000, department);

        List<String> expectedVacancyIDs = Arrays.asList(
                v1.getId().toString(),
                v2.getId().toString(),
                v3.getId().toString());

        Page<Vacancy> result = findVancancies(70000, null);
        List<Vacancy> resultsList = result.getContent();

        Assert.assertEquals("Expected results", expectedVacancyIDs.size(), resultsList.size());

        List<String> resultVacancyIDs = resultsList.stream()
                .map(v -> v.getId().toString())
                .collect(Collectors.toList());

        Assert.assertTrue(resultVacancyIDs.contains(v1.getId().toString()));
        Assert.assertTrue(resultVacancyIDs.contains(v2.getId().toString()));
        Assert.assertTrue(resultVacancyIDs.contains(v3.getId().toString()));
    }

    public Page<Vacancy> findVancancies(Integer minSalary, Integer maxSalary) throws Exception {

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

    private Vacancy createVacancyWithSalaryRange(Integer salaryMin, Integer salaryMax, Department department) {

        Vacancy vacancy = getVacancyPrototype();
        vacancy.setDepartment(department);
        vacancy.setSalaryMin(salaryMin);
        vacancy.setSalaryMax(salaryMax);
        vacancyRepository.save(vacancy);
        createdVacancies.add(vacancy);
        return vacancy;
    }
}
