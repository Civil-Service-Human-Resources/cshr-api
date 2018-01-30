package uk.gov.cshr.vcm.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests {@link SearchParameters}
 */
public class SearchParametersTest {
    private SearchParameters searchParameters;

    @Before
    public void setup() {
        Location location = Location.builder().place("location").radius(30).build();
        VacancySearchParameters vacancySearchParameters = VacancySearchParameters.builder().location(location).keyword("keyword").department(new String[]{"1"}).build();
        Coordinates coordinates = Coordinates.builder().latitude(0D).longitude(1D).build();

        searchParameters = SearchParameters.builder().vacancySearchParameters(vacancySearchParameters).coordinates(coordinates).build();
    }

    @After
    public void tearDown() {
        searchParameters = null;
    }

    @Test
    public void getDepartment(){
        assertThat(searchParameters.getDepartment(), equalTo(new String[]{"1"}));
    }

    @Test
    public void getKeyword(){
        assertThat(searchParameters.getKeyword(), equalTo("keyword"));
    }

    @Test
    public void getLocation(){
        assertThat(searchParameters.getLocation(), equalTo("location"));
    }

    @Test
    public void getRadius(){
        assertThat(searchParameters.getRadius(), equalTo(30));
    }

    @Test
    public void getLongitude(){
        assertThat(searchParameters.getLongitude(), equalTo(1D));
    }

    @Test
    public void getLatitude(){
        assertThat(searchParameters.getLatitude(), equalTo(0D));
    }
}
