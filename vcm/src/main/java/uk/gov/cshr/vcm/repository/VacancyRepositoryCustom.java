package uk.gov.cshr.vcm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;

/**
 * Defines the methods that will provide custom JPA queries for accessing the Vacancy datastore.
 */
public interface VacancyRepositoryCustom {
    /**
     * This method is responsible for creating and executing dynamic queries against the Vacancy entity's datastore.
     * <p>
     * The query will be built depending on the values supplied in the search parameters supplied
     *
     * @param vacancySearchParameters parameters and values supplied for conducting a search
     * @param pageable                Information supplied that dictate the pagination options available
     * @return Page<Vacancy> contains the results returned from the search query and data about pagination, such as total number results and number of results returned.
     */
    Page<Vacancy> search(VacancySearchParameters vacancySearchParameters, Pageable pageable);
}
