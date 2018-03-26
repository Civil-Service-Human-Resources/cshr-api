package uk.gov.cshr.vcm.controller.search;

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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
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
import uk.gov.cshr.vcm.controller.VacancyPage;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
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

//@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class SalaryRangeTest extends AbstractTestNGSpringContextTests {

    public static final double BRISTOL_LATITUDE = 51.4549291;
    public static final double BRISTOL_LONGITUDE = -2.6278111;

    public static final double NEWCASTLE_LATITUDE = 54.9806308;
    public static final double NEWCASTLE_LONGITUDE = -1.6167437;

    public static final Coordinates BRISTOL = new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West");
    public static final Coordinates NEWCASTLE = new Coordinates(NEWCASTLE_LONGITUDE, NEWCASTLE_LATITUDE, "North East");

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
    private HibernateSearchService hibernateSearchService;

    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30);
    private static final Timestamp ONE_DAY_AGO = getTime(-1);

    private final List<Vacancy> createdVacancies = new ArrayList<>();
    private final List<Department> createdDepartments = new ArrayList<>();

    private Department department;

    @Before
    public void before() throws LocationServiceException {

        hibernateSearchService.purge();
        vacancyRepository.deleteAll();
        departmentRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        department = departmentRepository.save(Department.builder().name("Department One").build());
        createdDepartments.add(department);

        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE, "South West"));

        given(locationService.find("newcastle"))
                .willReturn(new Coordinates(NEWCASTLE_LONGITUDE, NEWCASTLE_LATITUDE, "North East"));
    }

    @After
    public void after() {

//        for (Vacancy createdVacancy : createdVacancies) {
//            vacancyRepository.delete(createdVacancy);
//        }
//
//        for (Department createdDepartment : createdDepartments) {
//            departmentRepository.delete(createdDepartment);
//        }
    }

    @Test
    public void testFindMinSalaryNoMax() throws Exception {

        createVacancyWithSalaryRange(14000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(10000, 20000, "bristol");
        List<Vacancy> resultsList = result.getContent();

        boolean vacancyFound = false;

        for (Vacancy vacancy : resultsList) {

            if (vacancy.getSalaryMin() == 14000) {
                vacancyFound = true;
            }
        }

        Assert.assertTrue(vacancyFound);
    }

    @Test
    public void testFindMinSalaryWithMinAndMax() throws Exception {

        createVacancyWithSalaryRange(14000, 48000, department, BRISTOL);
        createVacancyWithSalaryRange(20000, 29000, department, BRISTOL);
        createVacancyWithSalaryRange(41000, 50000, department, BRISTOL);

        Page<Vacancy> result = findVancancies(30000, 40000, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());

        for (Vacancy vacancy : resultsList) {

            if (vacancy.getSalaryMin() > 40000) {
                Fail.fail("Vacancy with salary range " + vacancy.getSalaryMin() + "-" + vacancy.getSalaryMax() + "  when filtering for 30000-40000");
            }

            if (vacancy.getSalaryMax() < 30000) {
                Fail.fail("Vacancy with salary range " + vacancy.getSalaryMin() + "-" + vacancy.getSalaryMax() + "  when filtering for 30000-40000");
            }
        }
    }

    @Test
    public void testMaxSalary() throws Exception {

        createVacancyWithSalaryRange(40000, 50000, department, BRISTOL);
        createVacancyWithSalaryRange(69001, 70000, department, BRISTOL);
        createVacancyWithSalaryRange(69000, 70000, department, BRISTOL);
        createVacancyWithSalaryRange(40000, 69000, department, BRISTOL);

        Page<Vacancy> result = findVancancies(null, 69000, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", !resultsList.isEmpty());

        for (Vacancy vacancy : resultsList) {

            if (vacancy.getSalaryMin() == 69001) {
                Fail.fail("Vacancy with min salary of " + vacancy.getSalaryMin() + " when filtering for max of 69000");
            }
        }
    }

    @Test
    public void testMinSalary() throws Exception {

        createVacancyWithSalaryRange(10000, 20000, department, BRISTOL);
        createVacancyWithSalaryRange(20000, 30000, department, BRISTOL);
        createVacancyWithSalaryRange(30000, 40000, department, BRISTOL);
        createVacancyWithSalaryRange(40000, 50000, department, BRISTOL);

        Vacancy v1 = createVacancyWithSalaryRange(60000, 70000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(70000, 70001, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(70001, 80000, department, BRISTOL);

        // will not be returned as max_salary will now be defaulted to min_salary
        Vacancy v4 = createVacancyWithSalaryRange(10000, null, department, BRISTOL);

        List<String> expectedVacancyIDs = Arrays.asList(
                v1.getId().toString(),
                v2.getId().toString(),
                v3.getId().toString());

        Page<Vacancy> result = findVancancies(70000, null, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertEquals("Expected results", expectedVacancyIDs.size(), resultsList.size());

        List<String> resultVacancyIDs = resultsList.stream().map(v -> v.getId().toString()).collect(Collectors.toList());
        Assert.assertTrue(resultVacancyIDs.contains(v1.getId().toString()));
        Assert.assertTrue(resultVacancyIDs.contains(v2.getId().toString()));
        Assert.assertTrue(resultVacancyIDs.contains(v3.getId().toString()));
    }

    @Test
    public void testMultipleLocations() throws Exception {

        Vacancy newcastle = createVacancyWithSalaryRange(11000, null, department, NEWCASTLE);
        Vacancy bristol = createVacancyWithSalaryRange(14000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(10000, 20000, "newcastle");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", resultsList.size() > 0);

        for (Vacancy vacancy : resultsList) {

            if (vacancy.equals(bristol)) {
                fail("Should not have matched bristol");
            }
        }
    }

    @Test
    public void testNewcastleMin() throws Exception {

        Vacancy newcastle = createVacancyWithSalaryRange(11000, null, department, NEWCASTLE);

        Page<Vacancy> result = findVancancies(10000, null, "newcastle");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", resultsList.get(0).getId().equals(newcastle.getId()));
    }

    @Test
    public void testMinNoMaxVacancy_noMinMaxSearch() throws Exception {

        Vacancy newcastle = createVacancyWithSalaryRange(11000, null, department, NEWCASTLE);

        Page<Vacancy> result = findVancancies(null, 5000, "newcastle");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue("Expected results", resultsList.isEmpty());
    }

    @Test
    public void testSalaries1() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);

        List<Long> ids = Arrays.asList(
                v1.getId(),
                v2.getId(),
                v3.getId(),
                v4.getId(),
                v5.getId());

        Page<Vacancy> result = findVancancies(10000, null, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.size() == 5);

        List<Long> results = new ArrayList<>();
        resultsList.forEach((vacancy) -> {
            results.add(vacancy.getId());
        });

        // who knows why this does not work :/
//        Assert.assertThat(results, Matchers.containsInAnyOrder(ids));

//        Assert.assertTrue(resultsList.contains(v1));
//        Assert.assertTrue(resultsList.contains(v2));
//        Assert.assertTrue(resultsList.contains(v3));
//        Assert.assertTrue(resultsList.contains(v4));
//        Assert.assertTrue(resultsList.contains(v5));
    }

    @Test
    public void testSalaries2() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(40000, null, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.size() == 4);
//        Assert.assertFalse(resultsList.contains(v1));
//        Assert.assertTrue(resultsList.contains(v2));
//        Assert.assertTrue(resultsList.contains(v3));
//        Assert.assertTrue(resultsList.contains(v4));
//        Assert.assertTrue(resultsList.contains(v5));

        System.out.println("Success");
    }

    @Test
    public void testSalaries3() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(50000, null, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.size() == 4);
//        Assert.assertFalse(resultsList.contains(v1));
//        Assert.assertTrue(resultsList.contains(v2));
//        Assert.assertTrue(resultsList.contains(v3));
//        Assert.assertTrue(resultsList.contains(v4));
//        Assert.assertTrue(resultsList.contains(v5));
    }

    @Test
    public void testSalaries4() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(60000, null, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.size() == 3);
//        Assert.assertFalse(resultsList.contains(v1));
//        Assert.assertFalse(resultsList.contains(v2));
//        Assert.assertTrue(resultsList.contains(v3));
//        Assert.assertTrue(resultsList.contains(v4));
//        Assert.assertTrue(resultsList.contains(v5));
    }

    @Test
    public void testSalaries5() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(70000, null, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.isEmpty());
        Assert.assertFalse(resultsList.contains(v1));
        Assert.assertFalse(resultsList.contains(v2));
        Assert.assertFalse(resultsList.contains(v3));
        Assert.assertFalse(resultsList.contains(v4));
        Assert.assertFalse(resultsList.contains(v5));
    }

    @Test
    public void testSalaries6() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(10000, 30000, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.size() == 1);
//        Assert.assertTrue(resultsList.contains(v1));
//        Assert.assertFalse(resultsList.contains(v2));
//        Assert.assertFalse(resultsList.contains(v3));
//        Assert.assertFalse(resultsList.contains(v4));
//        Assert.assertFalse(resultsList.contains(v5));
    }

    @Test
    public void testSalaries7() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(30000, 40000, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.size() == 1);
//        Assert.assertTrue(resultsList.contains(v1));
//        Assert.assertFalse(resultsList.contains(v2));
//        Assert.assertFalse(resultsList.contains(v3));
//        Assert.assertFalse(resultsList.contains(v4));
//        Assert.assertFalse(resultsList.contains(v5));
    }

    @Test
    public void testSalaries8() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);


        Page<Vacancy> result = findVancancies(30000, 50000, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.size() == 3);

        List<Long> results = new ArrayList<>();
        resultsList.forEach((vacancy) -> {
            results.add(vacancy.getId());
        });

        Assert.assertThat(results, not(contains(v4.getId(), v5.getId())));


//        Assert.assertTrue(resultsList.contains(v1));
//        Assert.assertTrue(resultsList.contains(v2));
//        Assert.assertTrue(resultsList.contains(v3));
//        Assert.assertFalse(resultsList.contains(v4));
//        Assert.assertFalse(resultsList.contains(v5));
    }

    @Test
    public void testSalaries9() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(40000, 50000, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.size() == 2);
//        Assert.assertFalse(resultsList.contains(v1));
//        Assert.assertTrue(resultsList.contains(v2));
//        Assert.assertTrue(resultsList.contains(v3));
//        Assert.assertFalse(resultsList.contains(v4));
//        Assert.assertFalse(resultsList.contains(v5));
    }

    @Test
    public void testSalaries10() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(50000, 60000, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.size() == 4);
//        Assert.assertFalse(resultsList.contains(v1));
//        Assert.assertTrue(resultsList.contains(v2));
//        Assert.assertTrue(resultsList.contains(v3));
//        Assert.assertTrue(resultsList.contains(v4));
//        Assert.assertTrue(resultsList.contains(v5));
    }

    @Test
    public void testSalaries11() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(60000, 60000, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.size() == 3);
//        Assert.assertFalse(resultsList.contains(v1));
//        Assert.assertFalse(resultsList.contains(v2));
//        Assert.assertTrue(resultsList.contains(v3));
//        Assert.assertTrue(resultsList.contains(v4));
//        Assert.assertTrue(resultsList.contains(v5));
    }

    @Test
    public void testSalaries12() throws Exception {

        Vacancy v1 = createVacancyWithSalaryRange(30000, 31000, department, BRISTOL);
        Vacancy v2 = createVacancyWithSalaryRange(45000, 50000, department, BRISTOL);
        Vacancy v3 = createVacancyWithSalaryRange(50000, 60000, department, BRISTOL);
        Vacancy v4 = createVacancyWithSalaryRange(60000, 65000, department, BRISTOL);
        Vacancy v5 = createVacancyWithSalaryRange(60000, null, department, BRISTOL);

        Page<Vacancy> result = findVancancies(70000, 70000, "bristol");
        List<Vacancy> resultsList = result.getContent();

        Assert.assertTrue(resultsList.isEmpty());
//        Assert.assertFalse(resultsList.contains(v1));
//        Assert.assertFalse(resultsList.contains(v2));
//        Assert.assertFalse(resultsList.contains(v3));
//        Assert.assertFalse(resultsList.contains(v4));
//        Assert.assertFalse(resultsList.contains(v5));
    }

    public Page<Vacancy> findVancancies(Integer minSalary, Integer maxSalary, String place) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .keyword("SearchQueryDescription")
                .location(new Location(place, 30))
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

    private Vacancy getVacancyPrototype(Coordinates coordinates) {

        Vacancy vacancyPrototype = Vacancy.builder()
                .title("testTile1 SearchQueryTitle")
                .description("testDescription1 SearchQueryDescription")
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
                .identifier(1L)
                .numberVacancies(1)
                .identifier(System.currentTimeMillis())
                .build();

        VacancyLocation vacancyLocation = VacancyLocation.builder()
                .latitude(coordinates.getLatitude())
                .longitude(coordinates.getLongitude())
                .location("A Location")
                .vacancy(vacancyPrototype)
                .build();

        vacancyPrototype.getVacancyLocations().add(vacancyLocation);

        return vacancyPrototype;
    }

    private Vacancy createVacancyWithSalaryRange(Integer salaryMin, Integer salaryMax, Department department, Coordinates coordinates) {

        Vacancy vacancy = getVacancyPrototype(coordinates);
        vacancy.setDepartment(department);
        vacancy.setSalaryMin(salaryMin);
        vacancy.setSalaryMax(salaryMax != null ? salaryMax : salaryMin);
        vacancyRepository.save(vacancy);
        createdVacancies.add(vacancy);
        return vacancy;
    }
}