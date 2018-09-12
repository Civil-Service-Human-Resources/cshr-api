package uk.gov.cshr.vcm.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.junit.Test;

/**
 * Tests {@link VacancySortMethod}
 */
public class VacancySortMethodTest {
    @Test
    public void getSortField_CLOSING_DATE() {
        Sort expected = new Sort(new SortField( "vacancy.closingDate", SortField.Type.STRING, false ) );

        assertThat(VacancySortMethod.CLOSING_DATE.getSort(), is(equalTo(expected)));
    }

    @Test
    public void getSortField_RECENTLY_ADDED() {
        Sort expected = new Sort(new SortField( "vacancy.publicOpeningDate", SortField.Type.STRING, true ) );

        assertThat(VacancySortMethod.RECENTLY_ADDED.getSort(), is(equalTo(expected)));
    }
}
