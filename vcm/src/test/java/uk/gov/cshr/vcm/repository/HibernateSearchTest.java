package uk.gov.cshr.vcm.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.service.HibernateSearchService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class HibernateSearchTest extends AbstractTestNGSpringContextTests {

    public static final double BRISTOL_LATITUDE = 51.4549291;
    public static final double BRISTOL_LONGITUDE = -2.6278111;

    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30);
    private static final Timestamp ONE_DAY_AGO = getTime(-1);

    @Autowired
    private VacancyRepository vacancyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Inject
    private HibernateSearchService hibernateSearchService;

    private final List<Vacancy> createdVacancies = new ArrayList<>();
    private final List<Department> createdDepartments = new ArrayList<>();

    private Department department;

    @Before
    public void before() throws LocationServiceException {

        department = departmentRepository.save(Department.builder().name("Department One").build());
        createdDepartments.add(department);
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

//    @Test
//    public void testSearchVacancy() throws LocationServiceException {
//
//        Vacancy vacancy = getVacancyPrototype();
//        vacancy.setTitle("engineer");
//        vacancy.setSalaryMax(10001);
//        saveVacancy(vacancy);
//
//        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
//                .location(Location.builder().place("Bristol").radius(30).build())
//                .keyword("engineer")
//                .build();
//
//        Page<Vacancy> results = hibernateSearchService.search(vacancySearchParameters, null);
//        Assert.assertFalse(results.getContent().isEmpty());
//        Assert.assertEquals(vacancy.getId(), results.getContent().get(0).getId());
//    }

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
            .identifier(1L)
            .role("bacon")
            .numberVacancies(1)
                .latitude(BRISTOL_LATITUDE)
                .longitude(BRISTOL_LONGITUDE)
                .build();

        return vacancyPrototype;

}

    private Vacancy saveVacancy(Vacancy vacancy) {

        vacancy.setDepartment(department);
        vacancyRepository.save(vacancy);
        createdVacancies.add(vacancy);
        return vacancy;
    }


    private static Timestamp getTime(int numberOfDaysFromNow) {
        Date date = Date.from(LocalDateTime.now().plusDays(numberOfDaysFromNow).atZone(ZoneId.systemDefault()).toInstant());
        return new Timestamp(date.getTime());
    }
}
