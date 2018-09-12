package uk.gov.cshr.vcm.model;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

/**
 * This enum defines the possible sort methods available during search
 *
 * <pre>
 *     <ul>
 *         <li>CLOSING_DATE - will order results by ascending Vacancy.closingDate</li>
 *         <li>RECENTLY_ADDED - will order results by descending Vacancy.publicOpeningDate</li>
 *     </ul>
 * </pre>
 */
public enum VacancySortMethod {
    CLOSING_DATE(new Sort(new SortField("vacancy.closingDate", SortField.Type.STRING, false))),
    RECENTLY_ADDED(new Sort(new SortField("vacancy.publicOpeningDate", SortField.Type.STRING, true)));

    private Sort sort;

    VacancySortMethod(Sort sort) {
        this.sort = sort;
    }

    /**
     * Gets the sort object for this sort method
     *
     * @return sort object for this sort method
     */
    public Sort getSort() {
        return sort;
    }
}
