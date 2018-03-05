package uk.gov.cshr.vcm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;

@Service
public class HibernateSearchService {

    @Autowired
    private final EntityManager entityManager;

    @Autowired
    public HibernateSearchService(EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    public void initializeHibernateSearch() {

        try {
            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
            fullTextEntityManager.createIndexer().startAndWait();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public Page<Vacancy> search(VacancySearchParameters vacancySearchParameters, Pageable pageable) throws LocationServiceException {

        String searchTerm = vacancySearchParameters.getKeyword();

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Vacancy.class).get();
        Query fuzzyQuery = qb.keyword().fuzzy()
                .withEditDistanceUpTo(1)
                .withPrefixLength(1)
                .onFields("title", "description", "shortDescription")
                .matching(searchTerm).createQuery();

//                if (searchParameters.getSalaryMin() != null) {
//            query.append(" AND coalesce(salary_max, salary_min) >= :salary_min");
//        }
//
//        if (searchParameters.getSalaryMax() != null) {
//            query.append(" AND salary_min <= :salary_max");
//        }
//look for 0 <= starred < 3
        Integer minSalary = vacancySearchParameters.getMinSalary();
        Integer maxSalary = vacancySearchParameters.getMaxSalary();

        Query maxQuery = qb
                .range()
                .onField("salaryMax")
                .from(minSalary).to(maxSalary).excludeLimit()
                .createQuery();

        Query minQuery = qb
                .range()
                .onField("salaryMin")
                .from(0).to(maxSalary).excludeLimit()
                .createQuery();

//look for myths strictly BC
//        Date beforeChrist = ...;
// query.append(" FROM vacancies WHERE public_opening_date IS NOT NULL AND public_opening_date <= current_timestamp");
        Query openQuery = qb
                .range()
                .onField("publicOpeningDate")
                .below(new Date()).excludeLimit()
                .createQuery();

        Query closedQuery = qb
                .range()
                .onField("closingDate")
                .above(new Date()).excludeLimit()
                .createQuery();

        Query luceneQuery = qb
                .bool()
                .must(fuzzyQuery)
                .must(maxQuery)
                .must(minQuery)
                //                .must(openQuery)
                //                .must(closedQuery)
                .createQuery();

        System.out.println("luceneQuery=" + luceneQuery);

        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, Vacancy.class);

        // execute search
        try {
            List<Vacancy> results = jpaQuery.getResultList();
            PageImpl<Vacancy> page = new PageImpl<>(results, pageable, 1);
//            page.

            return page;
        }
        catch (NoResultException nre) {
            return new PageImpl<>(new ArrayList<>());

        }
    }
}
