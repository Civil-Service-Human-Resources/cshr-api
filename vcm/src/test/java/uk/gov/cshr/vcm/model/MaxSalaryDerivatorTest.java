package uk.gov.cshr.vcm.model;

import static org.junit.Assert.assertEquals;

import org.testng.annotations.Test;
import uk.gov.cshr.vcm.model.fixture.VacancyFixture;

/**
 * Tests {@link MaxSalaryDerivator}
 */
public class MaxSalaryDerivatorTest {
    @Test
    public void deriveMaxSalary_maxSalaryExists() {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        Integer maxSalary = 2000;
        vacancy.setSalaryMax(maxSalary);

        assertEquals("The value of the max salary property is incorrect", maxSalary, MaxSalaryDerivator.deriveMaxSalary(vacancy).getSalaryMax());
    }

    @Test
    public void deriveMaxSalary_noMaxSalaryExists() {
        Vacancy vacancy = VacancyFixture.getInstance().getPrototype();
        vacancy.setSalaryMax(null);
        vacancy.setSalaryMin(20000);
        Integer expected = 20000;

        assertEquals("The value of the max salary property is incorrect", expected, MaxSalaryDerivator.deriveMaxSalary(vacancy).getSalaryMax());
    }
}
