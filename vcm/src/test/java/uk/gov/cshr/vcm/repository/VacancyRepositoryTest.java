package uk.gov.cshr.vcm.repository;

import java.util.List;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.model.ApplicantTrackingSystemVendor;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.fixture.VacancyFixture;

/**
 * Tests queries in {@link VacancyRepository}
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VcmApplication.class)
@ContextConfiguration
public class VacancyRepositoryTest {
    
    private static final String ATS_OO1 = "ATS_OO1";
    private static final String ATS_OO2 = "ATS_OO2";
    
    public static final long JOB_REF_1234 = 1234L;
    public static final long JOB_REF_3234 = 3234L;

    @Inject
    private ApplicantTrackingSystemVendorRepository applicantTrackingSystemVendorRepository;

    @Inject
    private VacancyRepository vacancyRepository;

    private Vacancy vacancy1;

    @Inject
    private DepartmentRepository departmentRepository;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        applicantTrackingSystemVendorRepository.save(createATSVendor(ATS_OO1, "ATS Vendor 1"));
        applicantTrackingSystemVendorRepository.save(createATSVendor(ATS_OO2, "ATS Vendor 2"));

        vacancy1 = vacancyRepository.save(createVacancy(JOB_REF_1234, ATS_OO1));
        vacancyRepository.save(createVacancy(2234L, ATS_OO1));
        vacancyRepository.save(createVacancy(JOB_REF_3234, ATS_OO2));
    }

    @After
    public void tearDown() {
        applicantTrackingSystemVendorRepository.deleteAll();
        vacancyRepository.deleteAll();
    }

    private ApplicantTrackingSystemVendor createATSVendor(String clientIdentifier, String name) {
        return ApplicantTrackingSystemVendor.builder().clientIdentifier(clientIdentifier).name(name).build();
    }

    private Vacancy createVacancy(Long jobRef, String vendorIdentifier) {

        Department department1 = departmentRepository.save(Department.builder().id(1L).name("Department One").build());

        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        vacancy.setDepartment(department1);
        vacancy.setIdentifier(jobRef);
        vacancy.setAtsVendorIdentifier(vendorIdentifier);

        return vacancy;
    }

    @Test
    public void testCreateVacancy_nullDepartment() {

        Vacancy vacancy = createVacancy(JOB_REF_1234, ATS_OO1);
        vacancy.setDepartment(null);

        expectedException.expect(ConstraintViolationException.class);
        vacancyRepository.save(vacancy);
    }

    @Test
    public void testFindVacancy_noMatchingJobRef() {
        assertThat(vacancyRepository.findVacancy(JOB_REF_3234, ATS_OO1), is(empty()));
    }

    @Test
    public void testFindVacancy_noMatchingVendorId() {
        assertThat(vacancyRepository.findVacancy(JOB_REF_1234, "DUMMY"), is(empty()));

    }

    @Test
    public void testFindVacancy() {
        List<Vacancy> vacancies = vacancyRepository.findVacancy(JOB_REF_1234, ATS_OO1);

        assertThat(vacancies.size(), is(equalTo(1)));
        assertThat(vacancies.get(0).getId(), is(equalTo(vacancy1.getId())));
        assertThat(vacancies.get(0).getIdentifier(), is(equalTo(JOB_REF_1234)));
        assertThat(vacancies.get(0).getAtsVendorIdentifier(), is(equalTo(ATS_OO1)));
    }
}
