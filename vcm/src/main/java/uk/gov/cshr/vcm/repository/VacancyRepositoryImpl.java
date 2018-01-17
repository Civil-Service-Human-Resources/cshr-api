package uk.gov.cshr.vcm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;

/**
 * This class is responsible for providing custom dynamic JPA queries for working with Vacancies
 */
public class VacancyRepositoryImpl implements VacancyRepositoryCustom {
    private static final String DEPT = "dept";
    private static final String KEYWORD = "keyword";
    private static final String LOCATION = "location";
    private static final String WILDCARD = "%";

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Vacancy> search(VacancySearchParameters vacancySearchParameters, Pageable pageable) {
        Query selectQuery = em.createNativeQuery(buildQuery("SELECT *", vacancySearchParameters), Vacancy.class);
        Query countQuery = em.createNativeQuery(buildQuery("SELECT count(*)", vacancySearchParameters));

        selectQuery.setParameter(LOCATION, WILDCARD + vacancySearchParameters.getLocation() + WILDCARD);
        countQuery.setParameter(LOCATION, WILDCARD + vacancySearchParameters.getLocation() + WILDCARD);

        if (!StringUtils.isEmpty(vacancySearchParameters.getKeyword())) {
            selectQuery.setParameter(KEYWORD, WILDCARD + vacancySearchParameters.getKeyword() + WILDCARD);
            countQuery.setParameter(KEYWORD, WILDCARD + vacancySearchParameters.getKeyword() + WILDCARD);
        }

        if (!StringUtils.isEmpty(vacancySearchParameters.getDepartment())) {
            for (int i = 0; i < vacancySearchParameters.getDepartment().length; i++) {
                int deptId = Integer.valueOf(vacancySearchParameters.getDepartment()[i]);
                selectQuery.setParameter(DEPT + i, deptId);
                countQuery.setParameter(DEPT + i,  deptId);
            }
        }

        selectQuery.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize());

        List<Vacancy> vacancies = selectQuery.getResultList();

        BigInteger total = (BigInteger) countQuery.getSingleResult();

        return new PageImpl<>(vacancies, pageable, total.longValueExact());
    }

    private String buildQuery(String selectClause, VacancySearchParameters vacancySearchParameters) {
        StringBuilder query = new StringBuilder(selectClause);

        query.append(" FROM vacancies WHERE location ILIKE :location");

        if (!StringUtils.isEmpty(vacancySearchParameters.getKeyword())) {
            query.append(" AND CONCAT(title, ' ', description) ILIKE :keyword");
        }

        if (vacancySearchParameters.getDepartment() != null && vacancySearchParameters.getDepartment().length > 0) {
            System.out.println("INSIDE DEPT BUILDER. " + StringUtils.arrayToCommaDelimitedString(vacancySearchParameters.getDepartment()));
            query.append(" AND dept_id in (");

            for (int i = 0; i < vacancySearchParameters.getDepartment().length; i++) {
                query.append(":dept").append(i);
                if (i < vacancySearchParameters.getDepartment().length - 1) {
                    query.append(",");
                }
            }

            query.append(")");
        }

        return query.toString();
    }
}
