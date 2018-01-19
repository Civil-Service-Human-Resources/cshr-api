package model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import uk.gov.cshr.vcm.model.VacancySearchParameters;

/**
 * Tests {@link VacancySearchParameters}
 */
public class VacancySearchParametersTest {
    private static final String LOCATION = "location";
    private static final String DEPT1 = "dept1";
    private static final String DEPT2 = "dept2";
    private static final String KEYWORD = "keyword";

    @Test(expected = NullPointerException.class)
    public void build_noLocationSupplied() {
        VacancySearchParameters.builder().keyword(KEYWORD).build();
    }

    @Test
    public void build_locationSupplied() {
        VacancySearchParameters params = VacancySearchParameters.builder().location(LOCATION).build();

        assertThat(params.getLocation(), equalTo(LOCATION));
        assertThat(params.getKeyword(), is(nullValue()));
        assertThat(params.getDepartment(), is(nullValue()));
    }

    @Test
    public void build_locationAndKeywordSupplied() {
        VacancySearchParameters params = VacancySearchParameters.builder().location(LOCATION).keyword(KEYWORD).build();

        assertThat(params.getLocation(), equalTo(LOCATION));
        assertThat(params.getKeyword(), equalTo(KEYWORD));
        assertThat(params.getDepartment(), is(nullValue()));
    }

    @Test
    public void build_locationAndEmptyDepartmentSupplied() {
        VacancySearchParameters params = VacancySearchParameters.builder()
                .location(LOCATION)
                .department(new String[0])
                .build();

        assertThat(params.getLocation(), equalTo(LOCATION));
        assertThat(params.getKeyword(), is(nullValue()));
        assertThat(params.getDepartment(), is(notNullValue()));
        assertThat(params.getDepartment().length, equalTo(0));
    }

    @Test
    public void build_locationAndDepartmentSupplied() {
        VacancySearchParameters params = VacancySearchParameters.builder()
                .location(LOCATION)
                .department(new String[]{DEPT1, DEPT2})
                .build();

        assertThat(params.getLocation(), equalTo(LOCATION));
        assertThat(params.getKeyword(), is(nullValue()));
        assertThat(params.getDepartment()[0], equalTo(DEPT1));
        assertThat(params.getDepartment()[1], equalTo(DEPT2));
        assertThat(params.getDepartment().length, equalTo(2));
    }

    @Test
    public void build() {
        VacancySearchParameters params = VacancySearchParameters.builder()
                .location(LOCATION)
                .keyword(KEYWORD)
                .department(new String[]{DEPT1, DEPT2})
                .build();

        assertThat(params.getLocation(), equalTo(LOCATION));
        assertThat(params.getKeyword(), equalTo(KEYWORD));
        assertThat(params.getDepartment()[0], equalTo(DEPT1));
        assertThat(params.getDepartment()[1], equalTo(DEPT2));
        assertThat(params.getDepartment().length, equalTo(2));
    }
}
