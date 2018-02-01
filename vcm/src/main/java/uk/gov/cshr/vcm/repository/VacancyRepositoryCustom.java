package uk.gov.cshr.vcm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.Vacancy;

/**
 * Defines the methods that will provide custom JPA queries for accessing the Vacancy datastore.
 */
public interface VacancyRepositoryCustom {
    /**
     * This method is responsible for creating and executing dynamic queries against the Vacancy entity's data store.
     * <p>
     * The query will be built depending on the values supplied in the search parameters supplied
     *
     * @param searchParameters parameters and values supplied for conducting a search
     * @param pageable                Information supplied that dictate the pagination options available
     * @return Page<Vacancy> a page of vacancies that match the given search parameters and for which the maximum number of elements in the page is specified in Pageable.getPageSize()
     */
    Page<Vacancy> search(SearchParameters searchParameters, Pageable pageable);
}
