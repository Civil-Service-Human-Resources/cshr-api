package uk.gov.cshr.vcm.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests {@link VacancySearchParameters}
 */
public class VacancySearchParametersTest {
    private static final String PLACE = "location";
    private static final String DEPT1 = "dept1";
    private static final String DEPT2 = "dept2";
    private static final String KEYWORD = "keyword";

    private Location location;

    @Before
    public void setup() {
        location = Location.builder().place(PLACE).radius(30).build();
    }

    @After
    public void tearDown() {
        location = null;
    }

    @Test(expected = NullPointerException.class)
    public void build_noLocationSupplied() {
        VacancySearchParameters.builder().keyword(KEYWORD).build();
    }

    @Test
    public void build_locationSupplied() {

        VacancySearchParameters params = VacancySearchParameters.builder().location(location).build();

        assertThat(params.getLocation().getPlace(), equalTo(PLACE));
        assertThat(params.getLocation().getRadius(), equalTo(30));
        assertThat(params.getKeyword(), is(nullValue()));
        assertThat(params.getDepartment(), is(nullValue()));
    }

    @Test
    public void build_locationAndKeywordSupplied() {
        VacancySearchParameters params = VacancySearchParameters.builder().location(location).keyword(KEYWORD).build();

        assertThat(params.getLocation().getPlace(), equalTo(PLACE));
        assertThat(params.getLocation().getRadius(), equalTo(30));
        assertThat(params.getKeyword(), equalTo(KEYWORD));
        assertThat(params.getDepartment(), is(nullValue()));
    }

    @Test
    public void build_locationAndEmptyDepartmentSupplied() {
        VacancySearchParameters params = VacancySearchParameters.builder()
                .location(location)
                .department(new String[0])
                .build();

        assertThat(params.getLocation().getPlace(), equalTo(PLACE));
        assertThat(params.getLocation().getRadius(), equalTo(30));
        assertThat(params.getKeyword(), is(nullValue()));
        assertThat(params.getDepartment(), is(notNullValue()));
        assertThat(params.getDepartment().length, equalTo(0));
    }

    @Test
    public void build_locationAndDepartmentSupplied() {
        VacancySearchParameters params = VacancySearchParameters.builder()
                .location(location)
                .department(new String[]{DEPT1, DEPT2})
                .build();

        assertThat(params.getLocation().getPlace(), equalTo(PLACE));
        assertThat(params.getLocation().getRadius(), equalTo(30));
        assertThat(params.getKeyword(), is(nullValue()));
        assertThat(params.getDepartment()[0], equalTo(DEPT1));
        assertThat(params.getDepartment()[1], equalTo(DEPT2));
        assertThat(params.getDepartment().length, equalTo(2));
    }

    @Test
    public void build() {
        VacancySearchParameters params = VacancySearchParameters.builder()
                .location(location)
                .keyword(KEYWORD)
                .department(new String[]{DEPT1, DEPT2})
                .build();

        assertThat(params.getLocation().getPlace(), equalTo(PLACE));
        assertThat(params.getLocation().getRadius(), equalTo(30));
        assertThat(params.getKeyword(), equalTo(KEYWORD));
        assertThat(params.getDepartment()[0], equalTo(DEPT1));
        assertThat(params.getDepartment()[1], equalTo(DEPT2));
        assertThat(params.getDepartment().length, equalTo(2));
    }
}