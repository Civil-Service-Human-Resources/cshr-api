package uk.gov.cshr.vcm.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.cshr.vcm.model.SearchParameters;

/**
 * This class is responsible for building the native query that will be executed.
 */
@Component
public class SearchQueryBuilder {
    /**
     * This method is responsible for building a native sql count query used for pagination results in search.
     *
     * @param parameters data supplied used to perform the search
     * @return String native sql query to count the results of the search for pagination
     */
    public String buildCountQuery(SearchParameters parameters) {
        return buildQuery("SELECT count(*)", parameters);
    }

    private String buildQuery(String selectClause, SearchParameters searchParameters) {
        StringBuilder query = new StringBuilder(selectClause);

        query.append(" FROM vacancies WHERE public_opening_date IS NOT NULL AND public_opening_date <= :now");
        query.append(" AND (point(:searchFromLongitudeValue, :searchFromLatitudeValue) <@> point(longitude, latitude)) < :distance");

        if (StringUtils.isNotBlank(searchParameters.getKeyword())) {
            query.append(" AND CONCAT(title, ' ', description) ILIKE :keyword");
        }

        if (searchParameters.getDepartment() != null && searchParameters.getDepartment().length > 0) {
            query.append(buildDepartmentClause(searchParameters.getDepartment()));
        }

        if (searchParameters.getSalaryMin() != null) {
            query.append(" AND (salary_max >= :salary_min or salary_max is null)");
        }

        if (searchParameters.getSalaryMax() != null) {
            query.append(" AND (salary_min <= :salary_max )");
        }

        return query.toString();
    }

    private String buildDepartmentClause(String[] departments) {
        StringBuilder clause = new StringBuilder();
        boolean paramsFound = false;

        int paramNumber = 0;
        for (int i = 0; i < departments.length;i++) {
            if (StringUtils.isNotBlank(departments[i])) {
                paramsFound = true;
                clause.append(":dept").append(paramNumber++);
                if (i < departments.length - 1) {
                    clause.append(",");
                }
            }
        }

        if (paramsFound) {
            clause.insert(0," AND dept_id in (");
            clause.append(")");
        }

        return clause.toString();
    }

    public String buildSelectValuesQuery(SearchParameters parameters) {
        return buildQuery("SELECT *", parameters);
    }
}
