package uk.gov.cshr.vcm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.apache.commons.lang3.StringUtils;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for providing custom dynamic JPA queries for working with Vacancies
 */
public class VacancyRepositoryImpl implements VacancyRepositoryCustom {
    private static final String DEPT = "dept";
    private static final String DISTANCE = "distance";
    private static final String KEYWORD = "keyword";
    private static final String LOCATION = "location";
    private static final String NOW = "now";
    private static final String SEARCH_FROM_LATITUDE_VALUE = "searchFromLatitudeValue";
    private static final String SEARCH_FROM_LONGITUDE_VALUE = "searchFromLongitudeValue";
    private static final String WILDCARD = "%";

    @PersistenceContext
    private EntityManager em;

    @Inject
    private SearchQueryBuilder queryBuilder;

    /**
     * This method is responsible for building and executing a query to search for vacancies based on given search parameters.
     * <p>
     * If a vacancy has no value for Vacancy.publicOpeningDate this is treated as the vacancy is not open to the public.
     *
     * @param searchParameters parameters and values supplied for conducting a search
     * @param pageable                Information supplied that dictates the pagination options available
     * @return Page<Vacancy> a page of vacancies that match the given search parameters and for which the maximum number of elements in the page is specified in Pageable.getPageSize()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Page<Vacancy> search(SearchParameters searchParameters, Pageable pageable) {
        Query selectQuery = em.createNativeQuery(queryBuilder.buildSelectValuesQuery(searchParameters), Vacancy.class);
        Query countQuery = em.createNativeQuery(queryBuilder.buildCountQuery(searchParameters));

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        selectQuery.setParameter(NOW, now);
        countQuery.setParameter(NOW, now);

        selectQuery.setParameter(SEARCH_FROM_LONGITUDE_VALUE, searchParameters.getLongitude());
        selectQuery.setParameter(SEARCH_FROM_LATITUDE_VALUE, searchParameters.getLatitude());
        selectQuery.setParameter(DISTANCE, searchParameters.getRadius());
        countQuery.setParameter(SEARCH_FROM_LONGITUDE_VALUE, searchParameters.getLongitude());
        countQuery.setParameter(SEARCH_FROM_LATITUDE_VALUE, searchParameters.getLatitude());
        countQuery.setParameter(DISTANCE, searchParameters.getRadius());

        if (StringUtils.isNotBlank(searchParameters.getKeyword())) {
            selectQuery.setParameter(KEYWORD, WILDCARD + searchParameters.getKeyword() + WILDCARD);
            countQuery.setParameter(KEYWORD, WILDCARD + searchParameters.getKeyword() + WILDCARD);
        }

        if (searchParameters.getDepartment() != null && searchParameters.getDepartment().length > 0) {
            int paramNumber = 0;
            for (int i = 0; i < searchParameters.getDepartment().length; i++) {
                if (StringUtils.isNotBlank(searchParameters.getDepartment()[i])) {
                    int deptId = Integer.valueOf(searchParameters.getDepartment()[i]);
                    selectQuery.setParameter(DEPT + paramNumber, deptId);
                    countQuery.setParameter(DEPT + paramNumber, deptId);
                    paramNumber++;
                }
            }
        }

        selectQuery.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize());

        BigInteger total = (BigInteger) countQuery.getSingleResult();

        List<Vacancy> vacancies = total.compareTo(BigInteger.ZERO) > 0 ? selectQuery.getResultList() : new ArrayList<>();

        return new PageImpl<>(vacancies, pageable, total.longValueExact());
    }
}