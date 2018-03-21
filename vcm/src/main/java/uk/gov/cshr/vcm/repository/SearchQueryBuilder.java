package uk.gov.cshr.vcm.repository;

import org.apache.commons.lang3.BooleanUtils;
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

    public String buildSelectValuesQuery(SearchParameters parameters) {
        return buildQuery("SELECT *", parameters);
    }

    private String buildQuery(String selectClause, SearchParameters searchParameters) {
        StringBuilder query = new StringBuilder(selectClause);

        query.append(" FROM vacancies WHERE public_opening_date IS NOT NULL AND public_opening_date <= current_timestamp");

        query.append(" AND ((point(:searchFromLongitudeValue, :searchFromLatitudeValue) <@> point(longitude, latitude) < :distance)");

        if (StringUtils.isNotBlank(searchParameters.getCoordinates().getRegion())) {
            query.append(" OR (regions ILIKE :region))");
        }

        if (BooleanUtils.isFalse(searchParameters.getVacancySearchParameters().getOverseasJob())) {
            query.append(" AND (overseasjob != true or overseasjob is null )");
        }

        query.append(" AND closing_date > current_timestamp");

        if (StringUtils.isNotBlank(searchParameters.getVacancySearchParameters().getKeyword())) {
            query.append(" AND CONCAT(title, ' ', description) ILIKE :keyword");
        }

        if (searchParameters.getVacancySearchParameters().getDepartment() != null && searchParameters.getVacancySearchParameters().getDepartment().length > 0) {
            query.append(buildDepartmentClause(searchParameters.getVacancySearchParameters().getDepartment()));
        }

        if (searchParameters.getVacancySearchParameters().getMinSalary() != null) {
            query.append(" AND coalesce(salary_max, salary_min) >= :salary_min");
        }

        if (searchParameters.getVacancySearchParameters().getMaxSalary() != null) {
            query.append(" AND salary_min <= :salary_max");
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
}
