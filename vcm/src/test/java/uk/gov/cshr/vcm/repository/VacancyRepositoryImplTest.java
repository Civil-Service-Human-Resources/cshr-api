package uk.gov.cshr.vcm.repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.controller.SearchTestConfiguration;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.model.fixture.VacancyFixture;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VcmApplication.class)
@ContextConfiguration
public class VacancyRepositoryImplTest extends SearchTestConfiguration {
    private static final SimpleDateFormat ISO_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30, 0);
    private static final int TWENTY_DAYS_AGO = -20;
    private static final int TWENTY_DAYS_FROM_NOW = 20;
    public static final int THIRTY_MILES_FROM_PLACE = 30;
    public static final int PAGE_ONE = 0;
    public static final int MAX_TEN_RESULTS_PER_PAGE = 10;

    @Inject
    private VacancyRepository vacancyRepository;

    @Inject
    private DepartmentRepository departmentRepository;

    private Department department1;
    private Department department2;
    private Department department3;

    private Vacancy vacancy1;
    private Vacancy vacancy2;
    private Vacancy vacancy3;

    @Before
    public void setup() {
        createVacancy1();
        createVacancy2();
        createVacancy3();
    }

    private void createVacancy1() {
        department1 = departmentRepository.save(Department.builder().name("Department One").build());

        vacancy1 = VacancyFixture.getPrototype();
        vacancy1.setDepartment(department1);
        vacancy1.setDisplayCscContent(true);
        vacancy1.setIdentifier(398457345L);
        vacancy1.setLatitude(BRISTOL_LATITUDE);
        vacancy1.setLongitude(BRISTOL_LONGITUDE);
        vacancy1.setPublicOpeningDate(getTime(TWENTY_DAYS_AGO, 0));
        vacancy1.setSalaryMax(10);
        vacancy1.setSelectionProcessDetails("selectionProcessDetails");

        vacancy1 = this.vacancyRepository.save(vacancy1);
    }

    private void createVacancy2() {
        department2 = departmentRepository.save(Department.builder().name("Department Two").build());

        vacancy2 = Vacancy.builder()
                .identifier(398457346L)
                .title("testTitle2")
                .description("testDescription2")
                .location("Bristol")
                .grade("testGrade2")
                .role("testRole2")
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
                .latitude(BRISTOL_LATITUDE)
                .longitude(BRISTOL_LONGITUDE)
                .publicOpeningDate(getTime(TWENTY_DAYS_AGO, 0))
                .build();

        vacancy2 = this.vacancyRepository.save(vacancy2);
    }

    private void createVacancy3() {
        department3 = departmentRepository.save(Department.builder().name("Department Three").build());

        vacancy3 = Vacancy.builder()
                .identifier(398457347L)
                .title("testTitle3")
                .description("testDescription3")
                .location("Bristol")
                .grade("testGrade3")
                .role("testRole3")
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
                .latitude(BRISTOL_LATITUDE)
                .longitude(BRISTOL_LONGITUDE)
                .publicOpeningDate(getTime(TWENTY_DAYS_AGO, 0))
                .build();

        vacancy3 = this.vacancyRepository.save(vacancy3);
    }

    @After
    public void tearDown() {
        this.vacancyRepository.deleteAll();

        this.departmentRepository.deleteAll();
    }

    private static Timestamp getTime(int numberOfDaysFromNow, int hoursFromNow) {
        Date date = Date.from(LocalDateTime.now().plusDays(numberOfDaysFromNow).plusHours(hoursFromNow).atZone(ZoneId.systemDefault()).toInstant());

        return new Timestamp(date.getTime());
    }

    @Test
    public void testFindAll() {
        List<Vacancy> actual = (List<Vacancy>) vacancyRepository.findAll();

        assertThat(actual, hasSize(3));
        assertThat(actual.get(0), is(equalTo(vacancy1)));
        assertThat(actual.get(1), is(equalTo(vacancy2)));
        assertThat(actual.get(2), is(equalTo(vacancy3)));
    }

    @Test
    public void testFindById() {
        assertThat(vacancyRepository.findOne(vacancy1.getId()), is(equalTo(vacancy1)));
    }

    @Test
    public void testFindByIdNotFound() {
        assertThat(vacancyRepository.findOne(100000L), is(nullValue()));
    }

    @Test
    public void testUpdate() {
        vacancy1.setDescription("A new description for a brave new world");

        Vacancy actual = vacancyRepository.save(vacancy1);

        assertThat(actual, is(equalTo(vacancy1)));
    }

    @Test
    public void testDelete() {
        vacancyRepository.delete(vacancy1.getId());

        List<Vacancy> actual = (List<Vacancy>) vacancyRepository.findAll();

        assertThat(actual, hasSize(2));
        assertThat(actual.get(0), is(equalTo(vacancy2)));
        assertThat(actual.get(1), is(equalTo(vacancy3)));
    }

    @Test
    public void testFindAllPaginated_page1With1Result() {
        Page<Vacancy> vacancies = vacancyRepository.findAll(new PageRequest(0, 1));

        assertThat(vacancies.getTotalElements(), is(equalTo(Long.valueOf(3l))));
        assertThat(vacancies.getTotalPages(), is(3));
        assertThat(vacancies.getNumberOfElements(), is(1));
        assertThat(vacancies.getSize(), is(1));
        assertThat(vacancies.getContent().size(), is (1));
        assertThat(vacancies.getContent().get(0), is(equalTo(vacancy1)));
    }

    @Test
    public void search_locationAndKeywordSupplied() {
        SearchParameters searchParameters = SearchParameters.builder()
                .vacancySearchParameters(buildVacancySearchParamters("test", "bristol"))
                .coordinates(BRISTOL_COORDINATES)
                .build();

        Page<Vacancy> vacancies = vacancyRepository.search(searchParameters, new PageRequest(PAGE_ONE, MAX_TEN_RESULTS_PER_PAGE));

        assertThat(vacancies.getTotalElements(), is(equalTo(Long.valueOf(3l))));
        assertThat(vacancies.getTotalPages(), is(1));
        assertThat(vacancies.getNumberOfElements(), is(3));
        assertThat(vacancies.getSize(), is(10));
        assertThat(vacancies.getContent().size(), is (3));
        assertThat(vacancies.getContent().get(0), is(equalTo(vacancy1)));
        assertThat(vacancies.getContent().get(1), is(equalTo(vacancy2)));
        assertThat(vacancies.getContent().get(2), is(equalTo(vacancy3)));
    }

    private VacancySearchParameters buildVacancySearchParamters(String keyword, String place, String ... departmentIds ) {
        Location location = Location.builder().place(place).radius(THIRTY_MILES_FROM_PLACE).build();

        return VacancySearchParameters.builder()
                .keyword(keyword)
                .location(location)
                .department(departmentIds)
                .build();
    }

    @Test
    public void search_noResultsForlocationAndKeywordAndDepartment() {
        SearchParameters searchParameters = SearchParameters.builder()
                .vacancySearchParameters(buildVacancySearchParamters("test", "bristol", "100000"))
                .coordinates(BRISTOL_COORDINATES)
                .build();

        Page<Vacancy> vacancies = vacancyRepository.search(searchParameters, new PageRequest(PAGE_ONE, MAX_TEN_RESULTS_PER_PAGE));

        assertThat(vacancies.getTotalElements(), is(equalTo(Long.valueOf(0l))));
        assertThat(vacancies.getTotalPages(), is(0));
        assertThat(vacancies.getNumberOfElements(), is(0));
        assertThat(vacancies.getSize(), is(10));
        assertThat(vacancies.getContent().isEmpty(), is (true));
    }
}
