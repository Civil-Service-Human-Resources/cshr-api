package uk.gov.cshr.vcm.repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.sql.Timestamp;
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
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.model.fixture.VacancyFixture;

/**
 * Tests {@link VacancyRepositoryImpl}
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VcmApplication.class)
@ContextConfiguration
public class VacancyRepositoryImplTest extends AbstractJUnit4SpringContextTests {
    private static final double BRISTOL_LATITUDE = 51.4549291;
    private static final double BRISTOL_LONGITUDE = -2.6278111;
    private static final int MAX_TEN_RESULTS_PER_PAGE = 10;
    private static final int PAGE_ONE = 0;
    private static final int TEN_DAYS_AGO = -10;
    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30, 0);
    private static final int THIRTY_MILES_FROM_PLACE = 30;
    private static final int TWENTY_DAYS_AGO = -20;

    @Inject
    private VacancyRepository vacancyRepository;

    @Inject
    private DepartmentRepository departmentRepository;

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
        Department department1 = departmentRepository.save(Department.builder().name("Department One").build());

        vacancy1 = VacancyFixture.getInstance().getPrototype();
        vacancy1.setClosingDate(THIRTY_DAYS_FROM_NOW);
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
        Department department2 = departmentRepository.save(Department.builder().name("Department Two").build());

        vacancy2 = Vacancy.builder()
                .identifier(398457346L)
                .title("testTitle2")
                .description("testDescription2")
                .department(department2)
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
        Department department3 = departmentRepository.save(Department.builder().name("Department Three").build());

        vacancy3 = Vacancy.builder()
                .identifier(398457347L)
                .title("testTitle3")
                .description("testDescription3")
                .department(department3)
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

        assertThat(vacancies.getTotalElements(), is(equalTo(3L)));
        assertThat(vacancies.getTotalPages(), is(3));
        assertThat(vacancies.getNumberOfElements(), is(1));
        assertThat(vacancies.getSize(), is(1));
        assertThat(vacancies.getContent().size(), is (1));
        assertThat(vacancies.getContent().get(0), is(equalTo(vacancy1)));
    }

    @Test
    public void search_locationAndKeywordSupplied() {
        SearchParameters searchParameters = buildSearchParameters("test");

        Page<Vacancy> vacancies = vacancyRepository.search(searchParameters, new PageRequest(PAGE_ONE, MAX_TEN_RESULTS_PER_PAGE));

        assertThat(vacancies.getTotalElements(), is(equalTo(3L)));
        assertThat(vacancies.getTotalPages(), is(1));
        assertThat(vacancies.getNumberOfElements(), is(3));
        assertThat(vacancies.getSize(), is(10));
        assertThat(vacancies.getContent().size(), is (3));
        assertThat(vacancies.getContent().get(0), is(equalTo(vacancy1)));
        assertThat(vacancies.getContent().get(1), is(equalTo(vacancy2)));
        assertThat(vacancies.getContent().get(2), is(equalTo(vacancy3)));
    }

    private SearchParameters buildSearchParameters(String keyword, String ... departmentIds) {
        return SearchParameters.builder()
                .vacancySearchParameters(buildVacancySearchParameters(keyword, departmentIds))
                .coordinates(Coordinates.builder().latitude(BRISTOL_LATITUDE).longitude(BRISTOL_LONGITUDE).build())
                .build();
    }

    private VacancySearchParameters buildVacancySearchParameters(String keyword, String ... departmentIds ) {
        Location location = Location.builder().place("bristol").radius(THIRTY_MILES_FROM_PLACE).build();

        return VacancySearchParameters.builder()
                .keyword(keyword)
                .location(location)
                .department(departmentIds)
                .build();
    }

    @Test
    public void search_noResultsForlocationAndKeywordAndDepartment() {
        SearchParameters searchParameters = buildSearchParameters("test", "100000");

        doNoResultsExpectedTests(searchParameters);
    }

    private void doNoResultsExpectedTests(SearchParameters searchParameters) {
        Page<Vacancy> vacancies = vacancyRepository.search(searchParameters, new PageRequest(PAGE_ONE, MAX_TEN_RESULTS_PER_PAGE));

        assertThat(vacancies.getTotalElements(), is(equalTo(0L)));
        assertThat(vacancies.getTotalPages(), is(0));
        assertThat(vacancies.getNumberOfElements(), is(0));
        assertThat(vacancies.getSize(), is(10));
        assertThat(vacancies.getContent().isEmpty(), is (true));
    }

    @Test
    public void search_locationAndKeywordAndDepartment() {
        SearchParameters searchParameters = buildSearchParameters("test", String.valueOf(vacancy1.getDepartment().getId()));

        doReturnOneVacancyOnlyTests(searchParameters, vacancy1);
    }

    private void doReturnOneVacancyOnlyTests(SearchParameters searchParameters, Vacancy expectedVacacny) {
        Page<Vacancy> vacancies = vacancyRepository.search(searchParameters, new PageRequest(PAGE_ONE, MAX_TEN_RESULTS_PER_PAGE));

        assertThat(vacancies.getTotalElements(), is(equalTo(1L)));
        assertThat(vacancies.getTotalPages(), is(1));
        assertThat(vacancies.getNumberOfElements(), is(1));
        assertThat(vacancies.getSize(), is(10));
        assertThat(vacancies.getContent().size(), is (1));
        assertThat(vacancies.getContent().get(0), is(equalTo(expectedVacacny)));
    }

    @Test
    public void search_locationAndKeywordAndEmptyDepartment() {
        SearchParameters searchParameters = buildSearchParameters("SearchQueryTitle");

        doReturnOneVacancyOnlyTests(searchParameters, vacancy1);
    }

    @Test
    public void search_locationAndKeywordAndDepartments() {
        vacancy2.setDescription("testTitle " + vacancy2.getDescription());
        vacancy2 = vacancyRepository.save(vacancy2);

        String departmentOneId = String.valueOf(vacancy1.getDepartment().getId());
        String departmentTwoId = String.valueOf(vacancy2.getDepartment().getId());

        SearchParameters searchParameters = buildSearchParameters("testTitle", departmentOneId, departmentTwoId);

        Page<Vacancy> vacancies = vacancyRepository.search(searchParameters, new PageRequest(PAGE_ONE, MAX_TEN_RESULTS_PER_PAGE));

        assertThat(vacancies.getTotalElements(), is(equalTo(2L)));
        assertThat(vacancies.getTotalPages(), is(1));
        assertThat(vacancies.getNumberOfElements(), is(2));
        assertThat(vacancies.getSize(), is(10));
        assertThat(vacancies.getContent().size(), is (2));
        assertThat(vacancies.getContent().get(0), is(equalTo(vacancy1)));
        assertThat(vacancies.getContent().get(1), is(equalTo(vacancy2)));
    }

    @Test
    public void search_locationAndKeywordAndUnknownDepartment() {
        SearchParameters searchParameters = buildSearchParameters("testTitle", "1000000");

        doNoResultsExpectedTests(searchParameters);
    }

    @Test
    public void search_locationAndDepartment() {
        SearchParameters searchParameters = buildSearchParameters(null, String.valueOf(vacancy1.getDepartment().getId().toString()));

        doReturnOneVacancyOnlyTests(searchParameters, vacancy1);
    }

    @Test
    public void search_locationAndDepartmentAndUnknownKeyword() {
        SearchParameters searchParameters = buildSearchParameters("BlitheringEejit", String.valueOf(vacancy1.getDepartment().getId().toString()));

        doNoResultsExpectedTests(searchParameters);
    }

    @Test
    public void search_onlyInternalSearchesAllowed() {
        doClosedPublicSearchTests(TEN_DAYS_AGO, 10);
    }

    private void doClosedPublicSearchTests(Integer internalDateNumDaysFromNow, Integer governmentDateNumDaysFromNow) {
        if (governmentDateNumDaysFromNow != null) {
            vacancy3.setGovernmentOpeningDate(getTime(governmentDateNumDaysFromNow, 0));
        }

        if (internalDateNumDaysFromNow != null) {
            vacancy3.setInternalOpeningDate(getTime(internalDateNumDaysFromNow, 0));
        }

        vacancy3.setPublicOpeningDate(getTime(1, -1));

        this.vacancyRepository.save(vacancy3);

        SearchParameters searchParameters = buildSearchParameters("testTitle3", String.valueOf(vacancy3.getDepartment().getId().toString()));

        doNoResultsExpectedTests(searchParameters);
    }

    @Test
    public void search_internalAndGovtSearchesAllowed() {
        doClosedPublicSearchTests(TWENTY_DAYS_AGO, TEN_DAYS_AGO);
    }

    @Test
    public void search_noOpeningDatesSet(){
        vacancy3.setPublicOpeningDate(null);
        this.vacancyRepository.save(vacancy3);

        SearchParameters searchParameters = buildSearchParameters("testTitle3", String.valueOf(vacancy3.getDepartment().getId().toString()));

        doNoResultsExpectedTests(searchParameters);
    }

    @Test
    public void search_publicSearchesAllowedYesterday() {
        doOpenPublicSearchTests(-1, 0);
    }

    private void doOpenPublicSearchTests(int publicDateNumDaysFromNow, int numHoursFromNow) {
        vacancy3.setGovernmentOpeningDate(getTime(TEN_DAYS_AGO, 0));
        vacancy3.setInternalOpeningDate(getTime(TWENTY_DAYS_AGO, 0));
        vacancy3.setPublicOpeningDate(getTime(publicDateNumDaysFromNow, numHoursFromNow));

        vacancy3 = this.vacancyRepository.save(vacancy3);

        SearchParameters searchParameters = buildSearchParameters("testTitle3", String.valueOf(vacancy3.getDepartment().getId().toString()));

        doReturnOneVacancyOnlyTests(searchParameters, vacancy3);
    }

    @Test
    public void search_publicSearchesAllowedToday() {
        doOpenPublicSearchTests(0, -1);
    }
}
