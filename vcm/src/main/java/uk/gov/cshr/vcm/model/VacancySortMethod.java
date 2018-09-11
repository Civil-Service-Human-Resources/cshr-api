package uk.gov.cshr.vcm.model;

/**
 * This enum defines the possible sort methods available during search
 *
 * <pre>
 *     <ul>
 *         <li>CLOSING_DATE - will order results by descending Vacancy.closingDate</li>
 *         <li>RECENTLY_ADDED - will order results by descending Vacancy.publicOpeningDate</li>
 *     </ul>
 * </pre>
 */
public enum VacancySortMethod {
    CLOSING_DATE,
    RECENTLY_ADDED
}
