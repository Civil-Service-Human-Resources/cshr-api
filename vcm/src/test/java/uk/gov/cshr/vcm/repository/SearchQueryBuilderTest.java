package uk.gov.cshr.vcm.repository;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.VacancySearchParameters;

/**
 * Tests {@link SearchQueryBuilder}
 */
public class SearchQueryBuilderTest {

    private static final String ALL_LOCATION_PARAMETERS_QUERY_BODY = "/repository/AllLocationParametersQueryBody.sql";
    private static final String ALL_PARAMETERS_QUERY_BODY = "/repository/AllParametersSuppliedQueryBody.sql";
    private static final String BLANK = "";
    private static final String BRISTOL = "Bristol";
    private static final String[] DEPARTMENTS = {"1", "2"};
    private static final String KEYWORD = "keyword";
    private static final String LOCATION_DEPARTMENT_QUERY_BODY = "/repository/LocationAndDepartmentQueryBody.sql";
    private static final String LOCATION_KEYWORD_QUERY_BODY = "/repository/LocationAndKeywordQueryBody.sql";
    private static final String LOCATION_AND_OVERSEAS_QUERY_BODY = "/repository/LocationAndOverseasQueryBody.sql";
    private static final String NO_OVERSEAS_PARAMETER_QUERY_BODY = "/repository/NoOverseasParameterQueryBody.sql";

    private SearchQueryBuilder builder;

    @Before
    public void setup() {
        builder = new SearchQueryBuilder();
    }

    @After
    public void tearDown() {
        builder = null;
    }

    @Test
    public void buildCountQuery_onlyLocationSupplied() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, null, null, false, true, null, null), LOCATION_AND_OVERSEAS_QUERY_BODY);
    }

    private SearchParameters buildParameters(String place, String keyword, String[] department, boolean includeRegion, boolean includeOverseasJobs, Integer minSalary, Integer maxSalary) {
        Location location = Location.builder().place(place).radius(30).build();
        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .location(location)
                .keyword(keyword)
                .department(department)
                .overseasJob(includeOverseasJobs)
                .minSalary(minSalary)
                .maxSalary(maxSalary)
                .build();
        Coordinates coordinates = Coordinates.builder().latitude(51.4549291).longitude(-2.6278111).build();

        if (includeRegion) {
            coordinates.setRegion("South West");
        }

        return SearchParameters.builder().vacancySearchParameters(vacancySearchParameters).coordinates(coordinates).build();
    }

    private void doCountQueryTest(SearchParameters parameters, String expectedResourceFileName) throws IOException {

        String actualQuery = builder.buildCountQuery(parameters);

        try (InputStream inputStream = SearchQueryBuilderTest.class.getResourceAsStream(expectedResourceFileName)) {

            byte[] expectedQuery = ByteStreams.toByteArray(inputStream);
            assertThat(actualQuery, equalTo("SELECT count(*) " + new String(expectedQuery)));
        }
    }

    @Test
    public void buildCountQuery_locationSuppliedOthersEmpty() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, BLANK, new String[]{BLANK}, false, true, null, null), LOCATION_AND_OVERSEAS_QUERY_BODY);
    }

    @Test
    public void buildSelectValuesQuery_onlyLocationSupplied() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, null, null, false, true, null, null), LOCATION_AND_OVERSEAS_QUERY_BODY);
    }

    private void doSelectValuesQueryTest(SearchParameters parameters, String expectedResourceFileName) throws IOException {

        String actualQuery = builder.buildSelectValuesQuery(parameters);

        try (InputStream inputStream = SearchQueryBuilderTest.class.getResourceAsStream(expectedResourceFileName)) {

            byte[] expectedQuery = ByteStreams.toByteArray(inputStream);
            assertThat(actualQuery, equalTo("SELECT * " + new String(expectedQuery)));
        }
    }

    @Test
    public void buildSelectValuesQuery_locationSuppliedOthersEmpty() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, BLANK, new String[]{BLANK}, false, true, null, null), LOCATION_AND_OVERSEAS_QUERY_BODY);
    }

    @Test
    public void buildCountQuery_allLocationParametersRequired() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, null, null, true, true, null, null), ALL_LOCATION_PARAMETERS_QUERY_BODY);
    }

    @Test
    public void buildSelectValuesQuery_allLocationParametersRequired() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, null, null, true, true, null, null), ALL_LOCATION_PARAMETERS_QUERY_BODY);
    }

    @Test
    public void buildCountQuery_noOverseasJobsRequired() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, null, null, true, false, null, null), NO_OVERSEAS_PARAMETER_QUERY_BODY);
    }

    @Test
    public void buildSelectValuesQuery_noOverseasJobsRequired() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, null, null, true, false, null, null), NO_OVERSEAS_PARAMETER_QUERY_BODY);
    }

    @Test
    public void buildCountQuery_locationAndKeywordSupplied() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, KEYWORD, null, false, true, null, null), LOCATION_KEYWORD_QUERY_BODY);
    }

    @Test
    public void buildSelectValuesQuery_locationAndKeywordSupplied() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, KEYWORD, null, false, true, null, null), LOCATION_KEYWORD_QUERY_BODY);
    }

    @Test
    public void buildSelectValuesQuery_locationAndKeywordWithEmptyDepartment() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, KEYWORD, new String[]{BLANK}, false, true, null, null), LOCATION_KEYWORD_QUERY_BODY);
    }

    @Test
    public void buildCountQuery_locationAndKeywordWithEmptyDepartment() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, KEYWORD, new String[]{BLANK}, false, true, null, null), LOCATION_KEYWORD_QUERY_BODY);
    }

    @Test
    public void buildCountQuery_locationAndDepartmentSupplied() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, null, DEPARTMENTS, false, true, null, null), LOCATION_DEPARTMENT_QUERY_BODY);
    }

    @Test
    public void buildCountQuery_locationAndDepartmentWithEmptyKeyword() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, BLANK, DEPARTMENTS, false, true, null, null), LOCATION_DEPARTMENT_QUERY_BODY);
    }

    @Test
    public void buildSelectValuesQuery_locationAndDepartmentSupplied() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, null, DEPARTMENTS, false, true, null, null), LOCATION_DEPARTMENT_QUERY_BODY);
    }

    @Test
    public void buildSelectValuesQuery_locationAndDepartmentWithEmptyKeyword() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, BLANK, DEPARTMENTS, false, true, null, null), LOCATION_DEPARTMENT_QUERY_BODY);
    }

    @Test
    public void buildCountQuery_allParametersSupplied() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, KEYWORD, DEPARTMENTS, true, true, 10000, 20000), ALL_PARAMETERS_QUERY_BODY);
    }

    @Test
    public void buildSelectValuesQuery_allParametersSupplied() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, KEYWORD, DEPARTMENTS, true, true, 10000, 20000), ALL_PARAMETERS_QUERY_BODY);
    }

    @Test
    public void buildSelectValuesQuery_allParametersSuppliedWithRogueDepartment() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, KEYWORD, new String[]{"1", "   ", "2"}, true, true, 10000, 20000), ALL_PARAMETERS_QUERY_BODY);
    }
}
