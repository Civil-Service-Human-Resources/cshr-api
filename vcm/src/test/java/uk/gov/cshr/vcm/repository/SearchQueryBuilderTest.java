package uk.gov.cshr.vcm.repository;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.not;
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

    private static final String ALL_PARAMETERS_SELECT_VALUES_QUERY = "/repository/AllParametersSuppliedSelectValuesQuery.sql";
    private static final String BLANK = "";
    private static final String BRISTOL = "Bristol";
    private static final String[] DEPARTMENTS = {"1", "2"};
    private static final String KEYWORD = "keyword";
    private static final String LOCATION_DEPARTMENT_COUNT_QUERY = "/repository/LocationAndDepartmentCountQuery.sql";
    private static final String LOCATION_DEPARTMENT_SELECT_VALUES_QUERY = "/repository/LocationAndDepartmentSelectValuesQuery.sql";
    private static final String LOCATION_KEYWORD_COUNT_QUERY = "/repository/LocationAndKeywordCountQuery.sql";
    private static final String LOCATION_KEYWORD_SELECT_VALUES_QUERY = "/repository/LocationAndKeywordSelectValuesQuery.sql";
    private static final String LOCATION_COUNT_QUERY = "/repository/LocationOnlyCountQuery.sql";
    private static final String LOCATION_SELECT_VALUES_QUERY = "/repository/LocationOnlySelectValuesQuery.sql";
    private static final String SALARY_RANGE_QUERY = "/repository/SalaryRangeSelectValuesQuery.sql";

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
        doCountQueryTest(buildParameters(BRISTOL, null, null), LOCATION_COUNT_QUERY);
    }

    private SearchParameters buildParameters(String place, String keyword, String[] department) {
        Location location = Location.builder().place(place).radius(30).build();
        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder().location(location).keyword(keyword).department(department).build();
        Coordinates coordinates = Coordinates.builder().latitude(51.4549291).longitude(-2.6278111).build();

        return SearchParameters.builder().vacancySearchParameters(vacancySearchParameters).coordinates(coordinates).build();
    }

    private SearchParameters buildParameters(String place, String keyword, String[] department, Integer salaryMin, Integer salaryMax) {

        Location location = Location.builder()
                .place(place)
                .radius(30)
                .build();

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder()
                .location(location)
                .keyword(keyword)
                .department(department)
                .minSalary(salaryMin)
                .maxSalary(salaryMax)
                .build();

        Coordinates coordinates = Coordinates.builder()
                .latitude(51.4549291)
                .longitude(-2.6278111)
                .build();

        return SearchParameters.builder()
                .vacancySearchParameters(vacancySearchParameters)
                .coordinates(coordinates)
                .build();
    }

    private String doCountQueryTest(SearchParameters parameters, String expectedResourceFileName) throws IOException {

        String actualQuery = "";

        try (InputStream inputStream = SearchQueryBuilderTest.class.getResourceAsStream(expectedResourceFileName)) {

            byte[] expectedQuery = ByteStreams.toByteArray(inputStream);
            actualQuery = builder.buildCountQuery(parameters);
            assertThat(actualQuery, CoreMatchers.containsString(new String(expectedQuery)));;
        }

        return actualQuery;
    }

    @Test
    public void buildCountQuery_locationSuppliedOthersEmpty() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, BLANK, new String[]{BLANK}), LOCATION_COUNT_QUERY);
    }

    @Test
    public void buildSelectValuesQuery_onlyLocationSupplied() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, null, null), LOCATION_SELECT_VALUES_QUERY);
    }

    private String doSelectValuesQueryTest(SearchParameters parameters, String expectedResourceFileName)
            throws IOException {

        String actualQuery = "";

        try (InputStream inputStream = SearchQueryBuilderTest.class.getResourceAsStream(expectedResourceFileName)) {

            byte[] expectedQuery = ByteStreams.toByteArray(inputStream);

            actualQuery = builder.buildSelectValuesQuery(parameters);
            assertThat(actualQuery, CoreMatchers.startsWith(new String(expectedQuery)));
        }

        return actualQuery;
    }

    @Test
    public void buildSelectValuesQuery_locationSuppliedOthersEmpty() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, BLANK, new String[]{""}), LOCATION_SELECT_VALUES_QUERY);
    }

    @Test
    public void buildCountQuery_locationAndKeywordSupplied() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, KEYWORD, null), LOCATION_KEYWORD_COUNT_QUERY);
    }

    @Test
    public void buildCountQuery_locationAndKeywordWithEmptyDepartment() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, KEYWORD, new String[]{BLANK}), LOCATION_KEYWORD_COUNT_QUERY);
    }

    @Test
    public void buildSelectValuesQuery_locationAndKeywordSupplied() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, KEYWORD, null), LOCATION_KEYWORD_SELECT_VALUES_QUERY);
    }

    @Test
    public void buildSelectValuesQuery_locationAndKeywordWithEmptyDepartment() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, KEYWORD, new String[]{BLANK}), LOCATION_KEYWORD_SELECT_VALUES_QUERY);
    }

    @Test
    public void buildCountQuery_locationAndDepartmentSupplied() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, null, DEPARTMENTS), LOCATION_DEPARTMENT_COUNT_QUERY);
    }

    @Test
    public void buildCountQuery_locationAndDepartmentWithEmptyKeyword() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, BLANK, DEPARTMENTS), LOCATION_DEPARTMENT_COUNT_QUERY);
    }

    @Test
    public void buildSelectValuesQuery_locationAndDepartmentSupplied() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, null, DEPARTMENTS), LOCATION_DEPARTMENT_SELECT_VALUES_QUERY);
    }

    @Test
    public void buildSelectValuesQuery_locationAndDepartmentWithEmptyKeyword() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, BLANK, DEPARTMENTS), LOCATION_DEPARTMENT_SELECT_VALUES_QUERY);
    }

    @Test
    public void buildCountQuery_allParametersSupplied() throws IOException {
        doCountQueryTest(buildParameters(BRISTOL, KEYWORD, DEPARTMENTS), "/repository/AllParametersSuppliedCountQuery.sql");
    }

    @Test
    public void buildCountQuery_allParametersSuppliedWithSalaryRange() throws IOException {

        int salaryMin = 100000;
        int salaryMax = 200000;

        String generatedQuery = doCountQueryTest(buildParameters(BRISTOL, KEYWORD, DEPARTMENTS, salaryMin, salaryMax),
                "/repository/AllParametersSuppliedCountQuery.sql");

        String expectedClause = "AND salary_max >= :salary_min AND salary_min <= :salary_max or salary_max is null";
        assertThat(generatedQuery, CoreMatchers.containsString(expectedClause));
    }

    @Test
    public void buildCountQuery_allParametersSuppliedWithSalaryMin() throws IOException {

        int salaryMin = 100000;

        String generatedQuery = doCountQueryTest(buildParameters(BRISTOL, KEYWORD, DEPARTMENTS, salaryMin, null),
                "/repository/AllParametersSuppliedCountQuery.sql");

        String expectedClause = "AND salary_max >= :salary_min";
        String ommitedClause = " AND salary_min <= :salary_max or salary_max is null";

        assertThat(generatedQuery, CoreMatchers.containsString(expectedClause));
        assertThat(generatedQuery, not(CoreMatchers.containsString(ommitedClause)));
    }

    @Test
    public void buildCountQuery_allParametersSuppliedWithSalaryMax() throws IOException {

        int salaryMax = 100000;

        String generatedQuery = doCountQueryTest(buildParameters(BRISTOL, KEYWORD, DEPARTMENTS, null, salaryMax),
                "/repository/AllParametersSuppliedCountQuery.sql");

        String expectedClause = "salary_min <= :salary_max or salary_max is null";
        String ommitedClause = "salary_max >= :salary_min";

        assertThat(generatedQuery, CoreMatchers.containsString(expectedClause));
        assertThat(generatedQuery, not(CoreMatchers.containsString(ommitedClause)));
    }

    @Test
    public void buildSelectQuery_allParametersSuppliedWithSalaryRange() throws IOException {

        int salaryMin = 100000;
        int salaryMax = 200000;

        String generatedQuery = doSelectValuesQueryTest(buildParameters(BRISTOL, KEYWORD, DEPARTMENTS, salaryMin, salaryMax),
                "/repository/AllParametersSuppliedSelectValuesQuery.sql");

        String expectedClause = "AND salary_max >= :salary_min AND salary_min <= :salary_max or salary_max is null";
        assertThat(generatedQuery, CoreMatchers.containsString(expectedClause));
    }

    @Test
    public void buildSelectQuery_allParametersSuppliedWithSalaryMin() throws IOException {

        int salaryMin = 100000;

        String generatedQuery = doSelectValuesQueryTest(buildParameters(BRISTOL, KEYWORD, DEPARTMENTS, salaryMin, null),
                "/repository/AllParametersSuppliedSelectValuesQuery.sql");

        String expectedClause = "AND salary_max >= :salary_min";
        String ommitedClause = " AND salary_min <= :salary_max or salary_max is null";

        assertThat(generatedQuery, CoreMatchers.containsString(expectedClause));
        assertThat(generatedQuery, not(CoreMatchers.containsString(ommitedClause)));
    }

    @Test
    public void buildSelectQuery_allParametersSuppliedWithSalaryMax() throws IOException {

        int salaryMax = 100000;

        String generatedQuery = doSelectValuesQueryTest(buildParameters(BRISTOL, KEYWORD, DEPARTMENTS, null, salaryMax),
                "/repository/AllParametersSuppliedSelectValuesQuery.sql");

        String expectedClause = "salary_min <= :salary_max or salary_max is null";
        String ommitedClause = "salary_max >= :salary_min";

        assertThat(generatedQuery, CoreMatchers.containsString(expectedClause));
        assertThat(generatedQuery, not(CoreMatchers.containsString(ommitedClause)));
    }

    @Test
    public void buildSelectValuesQuery_allParametersSupplied() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, KEYWORD, DEPARTMENTS), ALL_PARAMETERS_SELECT_VALUES_QUERY);
    }

    @Test
    public void buildSelectValuesQuery_allParametersSuppliedWithRogueDepartment() throws IOException {
        doSelectValuesQueryTest(buildParameters(BRISTOL, KEYWORD, new String[]{"1", "   ", "2"}), ALL_PARAMETERS_SELECT_VALUES_QUERY);
    }

    @Test
    public void buildSelectValuesQuery_SalaryRange() throws IOException {

        VacancySearchParameters vacancySearchParameters = VacancySearchParameters
                .builder()
                .location(Location.builder().place(BRISTOL).radius(30).build())
                .minSalary(10000)
                .build();

        SearchParameters searchParameters = SearchParameters
                .builder()
                .vacancySearchParameters(vacancySearchParameters)
                .build();

        doSelectValuesQueryTest(searchParameters, SALARY_RANGE_QUERY);
    }
}
